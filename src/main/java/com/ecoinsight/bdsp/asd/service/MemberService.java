package com.ecoinsight.bdsp.asd.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.asd.authentication.Roles;
import com.ecoinsight.bdsp.core.Constants;
// import com.ecoinsight.bdsp.core.authentication.Roles;
import com.ecoinsight.bdsp.core.crypto.IEncryptor;
import com.ecoinsight.bdsp.core.entity.Member;
import com.ecoinsight.bdsp.core.entity.MemberRole;
import com.ecoinsight.bdsp.core.entity.Organization;
import com.ecoinsight.bdsp.core.entity.Role;
import com.ecoinsight.bdsp.core.model.NewMemberModel;
import com.ecoinsight.bdsp.core.repository.IMemberRepository;
import com.ecoinsight.bdsp.core.repository.IOrganizationRepository;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.repository.IRoleRepository;
import com.ecoinsight.bdsp.core.service.IMemberKeyGenerator;
import com.ecoinsight.bdsp.core.service.IPasswordGenerator;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.ecoinsight.bdsp.core.web.ApplicationErrorCodes;

public class MemberService extends com.ecoinsight.bdsp.core.service.MemberService {
    @Autowired private JavaMailSender _mailClient;
    @Value("${spring.mail.username}") private String _mailSenderAddress;

    @Autowired
    private PlatformTransactionManager _transactionManager;

    @Autowired
	private IMemberRepository _memberRepository;

    @Autowired
    private IProjectRepository _projectRepository;

	@Autowired
	private IOrganizationRepository _organizationRepository;

    @Autowired
    private IRoleRepository _roleRepository;
	
	@Autowired
    @Qualifier("memberKeyGenerator")
	private IMemberKeyGenerator _memberKeyGenerator;

	@Autowired
    @Qualifier("passwordGenerator")
	private IPasswordGenerator _passwordGenerator;

    @Autowired
    @Qualifier("passwordEncoder")
	private PasswordEncoder _passwordEncoder;

    @Autowired
	@Qualifier("stringEncryptor")
    private IEncryptor _encryptor;



    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public void sendPasswordResetMail(Member member) throws MailException, MessagingException {
		MimeMessage message = this._mailClient.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        
		helper.setSubject("아이AI 플랫폼 임시 비밀번호");
        helper.setFrom(_mailSenderAddress);
        helper.setTo(member.getEmail());

		StringBuilder body = new StringBuilder();
		body.append("아래의 정보로 로그인 한 후 비밀번호를 변경하기 바랍니다.\n\n");
        body.append("  - https://www.aiplatform101.com\n\n");
        body.append("  - 코드: ").append("ASD");
		body.append("  - 아이디: ").append(member.getMemberId());
		body.append("  - 임시 비밀번호: ").append(member.getPlainPassword());
		
        helper.setText(body.toString());         
        this._mailClient.send(message);
	}


    @Override
    public Member addNewMember(NewMemberModel model, String workerName) throws Exception {
    Optional<Organization> orgOptional = this._organizationRepository.findById(model.getOrganizationId(), model.getSystemId());
        if (orgOptional.isEmpty()) {
            LOGGER.error("No organization found. (Org={})", model.getOrganizationId());
            throw new ServiceException(
                String.format("기관정보가 존재하지 않습니다.. (Organization=%s)", model.getOrganizationId()), 
                ApplicationErrorCodes.ORGANIZATION_NO_FOUND);
        }

        Organization org = orgOptional.get();
		if (!org.isActiveRecord()) {
			LOGGER.error(String.format("Organization is not active. (Org=%s, Status=%s)", model.getOrganizationId(), org.getRecordStatus()));
            throw new ServiceException(
                String.format("기관의 상태가 사용가능한 상태가 아닙니다..(Organization=%s, Status=%s)", model.getOrganizationId(), org.getRecordStatus()),
                ApplicationErrorCodes.STATUS_NOT_ACTIVE);
		}
		if (!org.isClientAccessible()) {
			LOGGER.error(String.format("Organization is not allowed to users. (Org=%s, Usage=%s)", model.getOrganizationId(), org.getUsage()));
            throw new ServiceException(
                String.format("일반 사용자가 접근할 수 없는 기관입니다. (Organization=%s, Usage=%s)", model.getOrganizationId(), org.getUsage()),
                ApplicationErrorCodes.NOT_ACCESSIBLE_TO_CLIENT);
		}

        final List<Member> members = _memberRepository.findByMemberId(model.getUserId());
        final int total = members.size();
		final long count = members
                        .stream()
                        .filter(x-> x.getSystemId().equals(model.getSystemId())) // Member Id should be unique.
                        .count();
		if (count > 0) {
            final long removedCount = members.stream().filter(p->!p.isActiveRecord() || !p.isClientAccessible()).count();
			LOGGER.error(String.format("Users found with same user id. (System=%s, User=%s, Count=%s, UnavailableCount=%s)", model.getSystemId(), model.getUserId(), count, removedCount));

            if (removedCount > 0) {
                throw new ServiceException(String.format("동일한 아이디가 이미 존재합니다.\n(관리자에게 %s의 상태를 확인하세요.)", model.getUserId()), ApplicationErrorCodes.MEMBER_ALREADY_EXISTS_UNAVAILABLESTATUS);
            }
            else {
                throw new ServiceException(String.format("동일한 아이디가 이미 존재합니다. (User=%s)", model.getUserId()), ApplicationErrorCodes.MEMBER_ALREADY_EXISTS);
            }
		}
		
		String userName = model.getUserName();
		if (!StringUtils.hasText(userName)) {
			userName = model.getUserId();
			LOGGER.warn("No username provided. Using user id as username. (user id={})", model.getUserId());
		}

		String password = model.getPassword();
		if (!StringUtils.hasText(password)) {
			password = this._passwordGenerator.random();
			LOGGER.warn("No password provided. Making random password");
		}
		String encrypted = _passwordEncoder.encode(password);

		final String memberKey = _memberKeyGenerator.generate(org.getSystemId(), total > 0 ? model.getUserId() + total : model.getUserId());
		LOGGER.debug("New member key generated. (user key={}, user id={})", memberKey, model.getUserId());
		
        TransactionStatus txStatus = this._transactionManager.getTransaction(new DefaultTransactionDefinition());

        Member member  = new Member(memberKey, model.getUserId(), userName, model.getOrganizationId());
        member.setSystemId(org.getSystemId());
        member.setPlainPassword(password);

        try {
            /*
            * Insert new member (T_Member)
            */
            member.setMemberName(userName);
            member.setPassword(encrypted);
            member.setEmail(_encryptor.encrypt(model.getEmail()));
            member.setGender(model.getGender());
            member.setLastActivityTime(null);
            member.setDateCreated(LocalDateTime.now());
            member.setUserCreated(workerName);
            member.setDateUpdated(LocalDateTime.now());
            member.setUserUpdated(workerName);
            member.setUsage(Constants.USAGE_USER);
            member.setRecordStatus(Constants.USER_STATUS_ACTIVE);

            this._memberRepository.add(member);
            LOGGER.info("New member created. (member={}/{},{}, email={})", member.getMemberKey(), member.getMemberId(), member.getMemberName(), member.getEmail());

            /*
            * Insert new member roles (T_MemberRole)
            */
            Optional<Role> roleOptional = this._roleRepository.findById(Roles.USER_ENDUSER, org.getSystemId());
            if (roleOptional.isEmpty()) {
                roleOptional = this._roleRepository.findById(Roles.USER_BASIC, org.getSystemId());
            }

            if (roleOptional.isEmpty()) {
                LOGGER.error(String.format("No basic user role found. (Org=%s, user=%s, roles=%s, %s)", model.getOrganizationId(), model.getUserId(), Roles.USER_ENDUSER, Roles.USER_BASIC));
                throw new ServiceException(
                    String.format("사용자에게 적용할 기본 권한이 존재하지 않습니다. (User=%s)", model.getUserId()),
                    ApplicationErrorCodes.MEMBER_NO_BASIC_ROLES);
            }
            
            var role = new MemberRole(memberKey, roleOptional.get().getRoleSeq());
            role.setDateCreated(LocalDateTime.now());
            role.setUserCreated(workerName);
            role.setUsage(Constants.USAGE_USER);
            this._memberRepository.addRole(role);

            LOGGER.info("New member role created. (member={}, role={})", role.getMemberKey(), roleOptional.get().getRoleId());

            this._transactionManager.commit(txStatus);
        }
        catch (Exception ex) {
            LOGGER.error(String.format("Failed to create new member, member role. (member=%s, role=%s)", member.getMemberKey(), Roles.USER_ENDUSER), ex);
            this._transactionManager.rollback(txStatus);
            throw ex;
        }

        return member;
    }



    // public void sendNewAddBoardNotificationEmail(Member member) throws MailException, MessagingException {
	// 	MimeMessage message = this._mailClient.createMimeMessage();
    //     MimeMessageHelper helper = new MimeMessageHelper(message);
        
	// 	helper.setSubject("아이AI 플랫폼 임시 비밀번호");
    //     helper.setFrom(_mailSenderAddress);
    //     helper.setTo(member.getEmail());

	// 	StringBuilder body = new StringBuilder();
	// 	body.append("아래의 정보로 로그인 한 후 비밀번호를 변경하기 바랍니다.\n\n");
    //     body.append("  - https://www.aiplatform101.com\n\n");
    //     body.append("  - 코드: ").append("ASD");
	// 	body.append("  - 아이디: ").append(member.getMemberId());
	// 	body.append("  - 임시 비밀번호: ").append(member.getPlainPassword());
		
    //     helper.setText(body.toString());         
    //     this._mailClient.send(message);
	// }


}
