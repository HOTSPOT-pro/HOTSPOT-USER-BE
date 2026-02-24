package hotspot.user.member.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.member.controller.port.FindMemberService;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.domain.mapper.MemberMapper;
import hotspot.user.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;

/**
 * FindMemberService 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMemberServiceImpl implements FindMemberService {

    private final MemberRepository memberRepository;
    private final PhoneDecryptor phoneDecryptor;

    @Override
    public MemberResponse findByIdAndEmail(Long id, String email) {
         // 1. 통합 조회 (JOIN 쿼리 실행)
        MemberDetailInfo detailInfo = memberRepository.findDetailByIdAndEmail(id, email)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. 서비스 단에서 복호화 수행
        String decryptedPhone = phoneDecryptor.decrypt(detailInfo.getPhone());

        // 3. 매퍼를 통해 응답 DTO로 변환
        return MemberMapper.toMemberResponse(detailInfo, decryptedPhone);
    }

}
