package com.society.serviceimpl;

import com.society.dto.NoticeDTO;
import com.society.entity.Notice;
import com.society.entity.Society;
import com.society.entity.User;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.NoticeRepository;
import com.society.repository.ResidentRepository;
import com.society.repository.SocietyRepository;
import com.society.repository.StaffRepository;
import com.society.repository.UserRepository;
import com.society.security.SecurityUtils;
import com.society.service.EmailService;
import com.society.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service @RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final SocietyRepository societyRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService;
    private final SecurityUtils securityUtils;

    @Override @Transactional
    public NoticeDTO createNotice(NoticeDTO dto, Long adminUserId) {
        Long societyId = securityUtils.getCurrentSocietyId();
        User admin = userRepository.findById(adminUserId).orElse(null);
        Society society = societyRepository.findById(societyId)
            .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        Notice notice = Notice.builder().title(dto.getTitle()).content(dto.getContent())
            .createdBy(admin).society(society).build();
        NoticeDTO saved = toDTO(noticeRepository.save(notice));

        // Notify every resident and staff member of the society by email.
        Stream<String> residentEmails = residentRepository.findAllBySociety_SocietyId(societyId)
            .stream().map(r -> r.getUser().getEmail());
        Stream<String> staffEmails = staffRepository.findAllBySociety_SocietyId(societyId)
            .stream().map(s -> s.getUser().getEmail());
        List<String> recipients = Stream.concat(residentEmails, staffEmails).collect(Collectors.toList());
        emailService.sendToAll(recipients,
            "New notice: " + dto.getTitle(),
            "A new notice has been posted for " + society.getName() + ":\n\n" +
            dto.getTitle() + "\n\n" +
            dto.getContent() + "\n\n" +
            "Log in to Smart Society to view all notices.\n\n" +
            "- " + society.getName()
        );

        return saved;
    }
    @Override public Page<NoticeDTO> getAllNotices(Pageable pageable) {
        return noticeRepository.findAllBySociety_SocietyIdOrderByCreatedAtDesc(securityUtils.getCurrentSocietyId(), pageable)
            .map(this::toDTO);
    }
    @Override public NoticeDTO getNoticeById(Long id) {
        return toDTO(findById(id));
    }
    @Override @Transactional
    public NoticeDTO updateNotice(Long id, NoticeDTO dto) {
        Notice notice = findById(id);
        notice.setTitle(dto.getTitle()); notice.setContent(dto.getContent());
        return toDTO(noticeRepository.save(notice));
    }
    @Override @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.delete(findById(id));
    }

    private Notice findById(Long id) {
        return noticeRepository.findByNoticeIdAndSociety_SocietyId(id, securityUtils.getCurrentSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
    }
    private NoticeDTO toDTO(Notice n) {
        return NoticeDTO.builder().noticeId(n.getNoticeId()).title(n.getTitle()).content(n.getContent())
            .createdAt(n.getCreatedAt()).updatedAt(n.getUpdatedAt())
            .createdByName(n.getCreatedBy() != null ? n.getCreatedBy().getName() : "Admin").build();
    }
}
