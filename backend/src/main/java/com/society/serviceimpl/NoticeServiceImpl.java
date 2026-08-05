package com.society.serviceimpl;

import com.society.dto.NoticeDTO;
import com.society.entity.Notice;
import com.society.entity.User;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.NoticeRepository;
import com.society.repository.UserRepository;
import com.society.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Override @Transactional
    public NoticeDTO createNotice(NoticeDTO dto, Long adminUserId) {
        User admin = userRepository.findById(adminUserId).orElse(null);
        Notice notice = Notice.builder().title(dto.getTitle()).content(dto.getContent()).createdBy(admin).build();
        return toDTO(noticeRepository.save(notice));
    }
    @Override public Page<NoticeDTO> getAllNotices(Pageable pageable) {
        return noticeRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDTO);
    }
    @Override public NoticeDTO getNoticeById(Long id) {
        return toDTO(noticeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id)));
    }
    @Override @Transactional
    public NoticeDTO updateNotice(Long id, NoticeDTO dto) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
        notice.setTitle(dto.getTitle()); notice.setContent(dto.getContent());
        return toDTO(noticeRepository.save(notice));
    }
    @Override @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.delete(noticeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id)));
    }
    private NoticeDTO toDTO(Notice n) {
        return NoticeDTO.builder().noticeId(n.getNoticeId()).title(n.getTitle()).content(n.getContent())
            .createdAt(n.getCreatedAt()).updatedAt(n.getUpdatedAt())
            .createdByName(n.getCreatedBy() != null ? n.getCreatedBy().getName() : "Admin").build();
    }
}
