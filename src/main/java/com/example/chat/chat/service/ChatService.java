package com.example.chat.chat.service;


import com.example.chat.auth.models.User;
import com.example.chat.auth.repositories.UserRepository;
import com.example.chat.chat.dto.*;
import com.example.chat.chat.model.Chat;
import com.example.chat.chat.model.ChatMember;
import com.example.chat.chat.model.Message;
import com.example.chat.chat.model.MessageStatus;
import com.example.chat.chat.repository.ChatMemberRepository;
import com.example.chat.chat.repository.ChatRepository;
import com.example.chat.chat.repository.MessageRepository;
import com.example.chat.friendship.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepo;
    private final ChatMemberRepository memberRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepository; // from auth module
    private final FriendshipService friendshipService;
    private final SimpMessagingTemplate messagingTemplate;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) return (User) principal;
        throw new RuntimeException("Unauthorized");
    }

    @Transactional
    public ChatDTO createChat(CreateChatDTO dto) {
        User me = getCurrentUser();

        // For PRIVATE chats check if one already exists
        if ("PRIVATE".equalsIgnoreCase(dto.getType())) {
            if (dto.getMemberIds() == null || dto.getMemberIds().size() != 1)
                throw new IllegalArgumentException("PRIVATE chat requires exactly 1 other member");
            Long otherId = dto.getMemberIds().get(0);
            List<Chat> existing = chatRepo.findPrivateChatBetween(me.getId(), otherId);
            if (!existing.isEmpty()) {
                Chat c = existing.get(0);
                return toChatDTO(c);
            }
        }

        Chat chat = Chat.builder()
                .type(dto.getType())
                .name(dto.getName())
                .createdBy(me.getId())
                .createdAt(Instant.now())
                .build();
        Chat saved = chatRepo.save(chat);

        if (saved.getMembers() == null) saved.setMembers(new HashSet<>());

        // When adding members, add them to the object too
        ChatMember selfMember = ChatMember.builder()
                .chatId(saved.getId()).userId(me.getId()).role("ADMIN").build();
        memberRepo.save(selfMember);
        saved.getMembers().add(selfMember);

        // add other members
        if (dto.getMemberIds() != null) {
            for (Long uid : dto.getMemberIds()) {
                // avoid duplicating creator
                if (Objects.equals(uid, me.getId())) continue;
                ChatMember member = ChatMember.builder()
                        .chatId(saved.getId()).userId(uid).role("MEMBER").build();
                memberRepo.save(member);
            }
        }

        return toChatDTO(saved);
    }

    public ChatDTO toChatDTO(Chat chat) {
        User me = getCurrentUser();
        String displayName = chat.getName();
        String avatarUrl = null;
        boolean readOnly = false;

        // 1. Safe Member Mapping (Fixes the NullPointerException)
        // We use an empty list if getMembers() is null
        List<ChatMemberDTO> memberDTOS = (chat.getMembers() == null)
                ? Collections.emptyList()
                : chat.getMembers().stream()
                .map(m -> {
                    ChatMemberDTO md = new ChatMemberDTO();
                    md.setUserId(m.getUserId());
                    md.setRole(m.getRole());
                    return md;
                }).collect(Collectors.toList());

        // 2. Logic for PRIVATE/DIRECT chats
        if ("PRIVATE".equalsIgnoreCase(chat.getType()) || "DIRECT".equalsIgnoreCase(chat.getType())) {

            // Ensure members exist before searching for the other user
            if (chat.getMembers() != null) {
                Long otherUserId = chat.getMembers().stream()
                        .map(ChatMember::getUserId)
                        .filter(id -> !id.equals(me.getId()))
                        .findFirst()
                        .orElse(null);

                if (otherUserId != null) {
                    Optional<User> otherUserOpt = userRepository.findById(otherUserId);
                    User otherUser = otherUserOpt.get();
                    displayName = (otherUser.getDisplayName() != null && !otherUser.getDisplayName().isEmpty())
                            ? otherUser.getDisplayName()
                            : otherUser.getUsername();
                    avatarUrl = otherUser.getAvatarUrl();
                    // Set readOnly if friendship is broken
                    if (!friendshipService.areFriends(me.getId(), otherUserId)) {
                        readOnly = true;
                    }
                }
            }
        }

        return ChatDTO.builder()
                .id(chat.getId())
                .type(chat.getType())
                .name(displayName)
                .avatarUrl(avatarUrl)
                .isReadOnly(readOnly)
                .createdAt(chat.getCreatedAt())
                .createdBy(chat.getCreatedBy())
                .members(memberDTOS)
                .lastMessageId(chat.getLastMessageId())
                .lastMessageTimestamp(chat.getLastMessageTimestamp() == null ?
                        null : Instant.ofEpochMilli(chat.getLastMessageTimestamp()))
                .build();
    }

    public List<ChatDTO> getChatsForCurrentUser() {
        User me = getCurrentUser();
        List<Chat> chats = chatRepo.findByMemberUserId(me.getId());
        return chats.stream().map(this::toChatDTO).collect(Collectors.toList());
    }

    @Transactional
    public void addMember(Long chatId, AddMemberDTO dto) {
        User me = getCurrentUser();
        // check if me is admin
        ChatMember meMember = memberRepo.findByChatIdAndUserId(chatId, me.getId())
                .orElseThrow(() -> new RuntimeException("Not a member"));
        if (!"ADMIN".equalsIgnoreCase(meMember.getRole()))
            throw new RuntimeException("Only admins can add members");

        // check user exists
        userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

        if (memberRepo.findByChatIdAndUserId(chatId, dto.getUserId()).isPresent())
            throw new RuntimeException("Already a member");

        ChatMember member = ChatMember.builder()
                .chatId(chatId)
                .userId(dto.getUserId())
                .role(dto.getRole() == null ? "MEMBER" : dto.getRole())
                .build();
        memberRepo.save(member);
    }

    @Transactional
    public void removeMember(Long chatId, Long userId) {
        User me = getCurrentUser();
        ChatMember meMember = memberRepo.findByChatIdAndUserId(chatId, me.getId())
                .orElseThrow(() -> new RuntimeException("Not a member"));
        // admins or self can remove
        if (!"ADMIN".equalsIgnoreCase(meMember.getRole()) && !Objects.equals(me.getId(), userId))
            throw new RuntimeException("Not allowed");

        memberRepo.deleteByChatIdAndUserId(chatId, userId);
    }

    // convenience: create message (this is used by websocket message flow normally)
    @Transactional // Ensures message save and chat update succeed or fail together
    public MessageDTO saveMessage(MessageDTO dto, Principal principal) {
        // 1. Get the CURRENT authenticated user (Security check)
        Authentication authentication = (Authentication) principal;
        User currentUser = (User) authentication.getPrincipal();

        // 2. Build the message using the SECURE sender ID
        Message m = Message.builder()
                .chatId(dto.getChatId())
                .senderId(currentUser.getId()) // Use authenticated ID, not DTO ID
                .content(dto.getContent())
                .mediaUrl(dto.getMediaUrl())
                .status(MessageStatus.SENT) // Initial status is always SENT
                .isDeleted(false)
                .timestamp(Instant.now())
                .build();

        Message saved = messageRepo.save(m);

        // 3. Update chat last message (Optimized)
        chatRepo.findById(saved.getChatId()).ifPresent(chat -> {
            chat.setLastMessageId(saved.getId());
            chat.setLastMessageTimestamp(saved.getTimestamp().toEpochMilli());
            chatRepo.save(chat);
        });

        // 4. Return the DTO (Mapped from the saved entity)
        return mapToDTO(saved);
    }

    private MessageDTO mapToDTO(Message m) {
        // Look up the username based on the senderId
        String senderName = userRepository.findById(m.getSenderId())
                .map(User::getUsername)
                .orElse("Unknown User");

        return MessageDTO.builder()
                .id(m.getId())
                .chatId(m.getChatId())
                .senderId(m.getSenderId())
                .senderName(senderName)
                .content(m.getContent())
                .mediaUrl(m.getMediaUrl())
                .status(m.getStatus())
                .isDeleted(m.isDeleted())
                .timestamp(m.getTimestamp())
                .build();
    }

    @Transactional
    public ChatDTO getOrCreatePrivateChat(Long targetUserId) {
        User me = getCurrentUser();

        List<Chat> existing = chatRepo.findPrivateChatBetween(me.getId(), targetUserId);
        if (!existing.isEmpty()) {
            return toChatDTO(existing.get(0));
        }

        if (!friendshipService.areFriends(me.getId(), targetUserId)) {
            throw new RuntimeException("You can only start chats with friends.");
        }

        Chat chat = Chat.builder()
                .type("PRIVATE")
                .name("Direct Message")
                .createdBy(me.getId())
                .createdAt(Instant.now())
                .members(new HashSet<>()) // Initialize here to be safe
                .build();

        Chat saved = chatRepo.save(chat);

        // 🟢 Pass the object 'saved' instead of just 'saved.getId()'
        saveMember(saved, me.getId(), "MEMBER");
        saveMember(saved, targetUserId, "MEMBER");

        // No re-fetch needed! toChatDTO will now see the members in 'saved'
        return toChatDTO(saved);
    }

    @Transactional
    public void markMessagesAsRead(Long chatId) {
        User me = getCurrentUser();

        // 1. Update all messages in this chat sent by others to 'READ'
        List<Message> unreadMessages = messageRepo.findByChatIdAndSenderIdNotAndStatusNot(
                chatId, me.getId(), MessageStatus.READ
        );

        for (Message msg : unreadMessages) {
            msg.setStatus(MessageStatus.READ);
        }

        messageRepo.saveAll(unreadMessages);

        // 2. 🟢 CRITICAL: Notify the SENDER via WebSocket so their ticks turn blue
        // For each message, we could send a status update event
        System.out.println("Check... READ Messages" + chatId.toString());
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/status",
                new StatusUpdateDTO(chatId, me.getId(), MessageStatus.READ));
    }

    @Transactional
    public ChatDTO createGroupChat(CreateGroupRequest request) {
        User me = getCurrentUser();

        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new RuntimeException("You must select at least one friend.");
        }

        Chat chat = Chat.builder()
                .type("GROUP")
                .name(request.getName())
                .createdBy(me.getId())
                .createdAt(Instant.now())
                .members(new HashSet<>())
                .build();

        Chat savedChat = chatRepo.save(chat);

        // 🟢 Use the updated saveMember helper
        saveMember(savedChat, me.getId(), "ADMIN");

        for (Long friendId : request.getMemberIds()) {
            saveMember(savedChat, friendId, "MEMBER");
        }

        return toChatDTO(savedChat);
    }

    // Helper method to keep code clean
    private void saveMember(Chat chat, Long userId, String role) {
        ChatMember member = ChatMember.builder()
                .chatId(chat.getId()) // Use the ID from the object
                .userId(userId)
                .role(role)
                .build();

        memberRepo.save(member);

        // Initialize the collection if it's null and add the member in-memory
        if (chat.getMembers() == null) {
            chat.setMembers(new HashSet<>());
        }
        chat.getMembers().add(member);
    }


}
