package com.ecoinsight.bdsp.asd;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecoinsight.bdsp.asd.service.AsdBaseApiService;
import com.ecoinsight.bdsp.asd.service.AsdOrganizationService;
import com.ecoinsight.bdsp.asd.service.FilePathGenerator;
import com.ecoinsight.bdsp.asd.service.ProjectService;
import com.ecoinsight.bdsp.asd.service.TaskProgressService;
import com.ecoinsight.bdsp.core.SystemEnv;
import com.ecoinsight.bdsp.core.authentication.AuthenticationErrorEntryPoint;
import com.ecoinsight.bdsp.core.authentication.IIdentityClaimManager;
import com.ecoinsight.bdsp.core.authentication.IdentityClaimManager;
import com.ecoinsight.bdsp.core.authentication.IdentityUserManager;
import com.ecoinsight.bdsp.core.authentication.JwtRequestFilter;
import com.ecoinsight.bdsp.core.authentication.JwtTokenManager;
import com.ecoinsight.bdsp.core.authentication.UsernameAndPasswordAuthenticationProvider;
import com.ecoinsight.bdsp.core.crypto.ConfigValueEncryptor;
import com.ecoinsight.bdsp.core.crypto.IEncryptor;
import com.ecoinsight.bdsp.core.crypto.SpringPasswordEncoder;
import com.ecoinsight.bdsp.core.crypto.StringEncryptor;
import com.ecoinsight.bdsp.core.repository.IOrganizationRepository;
import com.ecoinsight.bdsp.core.service.IMemberKeyGenerator;
import com.ecoinsight.bdsp.core.service.IMemberService;
import com.ecoinsight.bdsp.core.service.IOrganizationCodeGenerator;
import com.ecoinsight.bdsp.core.service.IOrganizationService;
import com.ecoinsight.bdsp.core.service.IPasswordGenerator;
import com.ecoinsight.bdsp.core.service.IProjectService;
import com.ecoinsight.bdsp.core.service.ISystemSettings;
import com.ecoinsight.bdsp.core.service.MemberKeyGenerator;
import com.ecoinsight.bdsp.asd.service.MemberService;
import com.ecoinsight.bdsp.core.service.OrganizationCodeGenerator;
import com.ecoinsight.bdsp.core.service.OrganizationService;
import com.ecoinsight.bdsp.core.service.PasswordGenerator;
import com.ecoinsight.bdsp.core.service.SystemSettings;

@Configuration
public class ComponentConfig<IAsdOrganizationRepository> {


	 @Bean(name="projectBaseApiService")
	public AsdBaseApiService asdBaseApiService() {

			return new AsdBaseApiService();
	}

	/**
	 * @return
	 */

	 @Bean(name="IOrganizationRepository")
	public AsdOrganizationService asdOrganizationService() {

			return new AsdOrganizationService();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new SpringPasswordEncoder();
	}

	@Bean
	public JwtTokenManager jwtTokenManager() {
		return new JwtTokenManager();
	}

    @Bean(name = "stringEncryptor")
	public IEncryptor stringEncryptor() {
		String password = SystemEnv.getVariable("APP_BDSP_ENCRYPT_PASSWORD");		
		if (password == null || password.isBlank()) {
			password = "guseowltlrtksdjqtpsxjCehd316gh";
		}

		return new StringEncryptor(password);
	}

	@Bean(name = "configValueEncryptor")
	public IEncryptor configValueEncryptor() {
		String password = SystemEnv.getVariable("APP_BDSP_CONFIG_PASSWORD");		
		if (password == null || password.isBlank()) {
			password = "thdvkrnqjqdnjsfh11rlf";
		}

		return new ConfigValueEncryptor(password);
	}

	@Bean()
	public JwtRequestFilter jwtRequestFilter() {
		return new JwtRequestFilter();
	}

	@Bean()
	public AuthenticationErrorEntryPoint authenticationErrorEntryPoint() {
		return new AuthenticationErrorEntryPoint();
	}

	@Bean()
	public AuthenticationProvider authenticationProvider() {
		return new UsernameAndPasswordAuthenticationProvider();
	}

	@Bean()
	public IProjectService projectService() {
		return new ProjectService();
	}

	@Bean()
	public IIdentityClaimManager identityClaimManager() {
		return new IdentityClaimManager();
	}

	@Bean()
	public UserDetailsService identityUserManager() {
		return new IdentityUserManager();
	}

	@Bean()
	public IMemberKeyGenerator memberKeyGenerator() {
		return new MemberKeyGenerator();
	}

	@Bean()
	public IMemberService memberService() {
		return new MemberService();
	}

	@Bean()
	public IOrganizationCodeGenerator organizationCodeGenerator() {
		return new OrganizationCodeGenerator();
	}

	@Bean()
	public IOrganizationService organizationService() {
		return new OrganizationService();
	}
	
	@Bean()
	public IPasswordGenerator passwordGenerator() {
		return new PasswordGenerator();
	}

	@Bean()
	public ISystemSettings systemSettings() {
		return new SystemSettings();
	}

	@Bean()
	public FilePathGenerator filePathGenerator() {
		return new FilePathGenerator();
	}

	@Bean()
	public TaskProgressService taskProgressService() {
		return new TaskProgressService();
	}
}
