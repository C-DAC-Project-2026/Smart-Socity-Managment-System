package com.society.service;
import com.society.dto.NoticeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface NoticeService {
    NoticeDTO createNotice(NoticeDTO dto, Long adminUserId);
    Page<NoticeDTO> getAllNotices(Pageable pageable);
    NoticeDTO getNoticeById(Long id);
    NoticeDTO updateNotice(Long id, NoticeDTO dto);
    void deleteNotice(Long id);
}
