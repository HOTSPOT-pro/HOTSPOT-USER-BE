package hotspot.user.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.policy.controller.port.FindAppBlockedService;
import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import hotspot.user.policy.domain.mapper.AppBlockedServiceMapper;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 앱 차단 서비스 조히 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindAppBlockedServiceImpl implements FindAppBlockedService {
    private final AppBlockedServiceRepository appBlockedServiceRepository;

    @Override
    public List<AppBlockedServiceResponse> findAll() {
        return appBlockedServiceRepository.findAll().stream()
                .map(AppBlockedServiceMapper::toAppBlockedServiceResponse)
                .toList();
    }
}
