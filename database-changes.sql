-- 2022.07.18
-- After porting TRR system

-- 영상관련 기능 삭제
delete from T_RoleFunction 
where FunctionId in ('IMAGING', 'DIST_IMAGING');

delete from T_ServiceModuleFunction 
where FunctionId in ('IMAGING', 'DIST_IMAGING');

delete from T_ResearchAreaPage
where ResearchAreaId = 'IMAGING';

delete from T_ResearchItem 
where ResearchAreaId in ('IMAGING', 'IMAGING_PET');

delete from T_ResearchArea 
where ResearchAreaId in ('IMAGING', 'IMAGING_PET');


-- 2022.07.10
-- 스케쥴러 로그 기록 컬럼 수정
alter table T_SchedulerLog
modify column UserExecuted varchar(200) null;


-- 2022.06.30
-- 연구과제 속성 관리 테이블
CREATE TABLE IF NOT EXISTS `T_ProjectAttribute` (
  `ProjectSeq` bigint unsigned NOT NULL,
  `AttributeName` varchar(40) NOT NULL COMMENT '연구과제 속성명',
  `Value` varchar(100) NOT NULL COMMENT '연구과제 속성값',
  `CUser` varchar(40) DEFAULT NULL,
  `CDate` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `UUser` varchar(40) DEFAULT NULL,
  `UDate` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `_Version` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '레코드 변경 여부를 체크하기 위한 버전',
  PRIMARY KEY (`ProjectSeq`, `AttributeName`),
  KEY `FK_T_ProjectAttribute_T_Project` (`ProjectSeq`),
  CONSTRAINT `FK_T_ProjectAttribute_T_Project` FOREIGN KEY (`ProjectSeq`) REFERENCES `T_Project` (`ProjectSeq`)
)
COMMENT="연구과제 속성"
ENGINE=InnoDB;

-- 연구과제 속성
INSERT INTO T_Code (CodeName, GroupName, SystemId, Value, Description, Note, RStatus, CUser, CDate, `_Usage`, `_Version`)
VALUES('DIAGNOSIS', 'PROJECT_TYPE', 'SEOUL_ASD', '진단, 예측', '연구과제 타입-진단, 예측', NULL, 'ACTIVE', 'kapju', CURRENT_TIMESTAMP, 'USER', CURRENT_TIMESTAMP);

INSERT INTO T_Code (CodeName, GroupName, SystemId, Value, Description, Note, RStatus, CUser, CDate, `_Usage`, `_Version`)
VALUES('TREATMENT', 'PROJECT_TYPE', 'SEOUL_ASD', '치료, 예방', '연구과제 타입-치료, 예방', NULL, 'ACTIVE', 'kapju', CURRENT_TIMESTAMP, 'USER', CURRENT_TIMESTAMP);


-- 2022.06.26
update T_Menu 
set Icon = '/images/gnb_ico3.png'
where MenuId = 'ANALISYS';

update T_Menu 
set Icon = '/images/gnb_ico6.png'
where MenuId = 'DATAMNG';

update T_Menu 
set Icon = '/images/gnb_ico5.png'
where MenuId = 'SETTING';

update T_Menu 
set Icon = '/images/gnb_ico9.png'
where MenuId = 'BOARD_QNA';

-- 2022.05.02
-- Refresh token (Authentication)
CREATE TABLE IF NOT EXISTS `T_MemberAuthToken` (
  `MemberKey` varchar(200) NOT NULL,
  `Token` varchar(256) NOT NULL,
  `ExpiryDate` TIMESTAMP NOT NULL,
  `CUser` varchar(40) DEFAULT NULL,
  `CDate` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `_Version` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '레코드 변경 여부를 체크하기 위한 버전',
  PRIMARY KEY (`MemberKey`),
  Unique(Token)
)
COMMENT="사용자 인증 토큰 관리"
ENGINE=InnoDB;

