--
-- 테이블의 덤프 데이터 `T_Role`
--

--2023.06.13
 -- asd role seql 모두 컬럼 값 변경 
UPDATE `T_Role` SET `RoleSeq` = 1,`RoleId` = 'SystemAdmin',`SystemId` = 'ASD',`Level` = 0,`Description` = 'System Admin',`RStatus` = 'ACTIVE',`CUser` = 'kapju',`CDate` = '2023-01-06 19:13:36',`UUser` = 'kapju',`UDate` = '2023-01-06 19:13:36',`_Usage` = 'SYSTEM',`_Version` = '2023-01-06 01:13:36' WHERE `T_Role`.`RoleSeq` = 1;
UPDATE `T_Role` SET `RoleSeq` = 2,`RoleId` = 'Admin',`SystemId` = 'ASD',`Level` = 1,`Description` = '총괄 관리자',`RStatus` = 'ACTIVE',`CUser` = 'kapju',`CDate` = '2023-01-06 19:13:36',`UUser` = 'kapju',`UDate` = '2023-01-06 19:13:36',`_Usage` = 'APPLICATION',`_Version` = '2023-01-06 01:13:36' WHERE `T_Role`.`RoleSeq` = 2;
UPDATE `T_Role` SET `RoleSeq` = 3,`RoleId` = 'GeneralManager',`SystemId` = 'ASD',`Level` = 10,`Description` = '총괄 매니저',`RStatus` = 'ACTIVE',`CUser` = 'kapju',`CDate` = '2023-01-06 19:13:36',`UUser` = 'kapju',`UDate` = '2023-01-06 19:13:36',`_Usage` = 'APPLICATION',`_Version` = '2023-06-14 18:08:31' WHERE `T_Role`.`RoleSeq` = 3;
UPDATE `T_Role` SET `RoleSeq` = 4,`RoleId` = 'Manager',`SystemId` = 'ASD',`Level` = 50,`Description` = '기관 매니저',`RStatus` = 'ACTIVE',`CUser` = 'kapju',`CDate` = '2023-01-06 19:13:37',`UUser` = 'kapju',`UDate` = '2023-01-06 19:13:37',`_Usage` = 'APPLICATION',`_Version` = '2023-06-14 18:08:31' WHERE `T_Role`.`RoleSeq` = 4;
UPDATE `T_Role` SET `RoleSeq` = 5,`RoleId` = 'Researcher',`SystemId` = 'ASD',`Level` = 50,`Description` = '기관 연구원',`RStatus` = 'ACTIVE',`CUser` = 'kapju',`CDate` = '2023-01-06 19:13:37',`UUser` = 'kapju',`UDate` = '2023-01-06 19:13:37',`_Usage` = 'APPLICATION',`_Version` = '2023-06-13 01:24:10' WHERE `T_Role`.`RoleSeq` = 5;
UPDATE `T_Role` SET `RoleSeq` = 6,`RoleId` = 'Viewer',`SystemId` = 'ASD',`Level` = 9999,`Description` = '기본AI기관(타기관연구원)',`RStatus` = 'ACTIVE',`CUser` = 'kapju',`CDate` = '2023-01-06 19:13:37',`UUser` = 'kapju',`UDate` = '2023-01-06 19:13:37',`_Usage` = 'APPLICATION',`_Version` = '2023-06-07 22:37:13' WHERE `T_Role`.`RoleSeq` = 6;

-- --------------------------------------------------------




--
-- 테이블의 덤프 데이터 `T_RoleMenu`
--




-- 2022.06.22 -운영반영시점
-- RoleSeq 기관 연구원, 연구원 상세 메뉴의 서브메뉴 role 권한 부여 

INSERT ignore INTO `T_RoleMenu` (`RoleSeq`, `MenuId`, `CUser`, `CDate`, `_Usage`, `_Version`) VALUES 
(5, 'ANALISYS', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(5, 'BOARD_QNA', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(5, 'TRIAL_ALL', 'kapju', '2023-01-06 19:16:59', 'USER', '2023-01-06 10:16:59'),
(5, 'TRIAL_BIOMARKER', 'ecomylee', '2023-06-13 18:58:36', 'USER', '2023-06-13 09:58:36'),
(5, 'TRIAL_DIGITALHEALTH', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(5, 'TRIAL_SELECTION', 'ecomylee', '2023-06-13 18:58:36', 'USER', '2023-06-13 09:58:36'),
(6, 'TRIAL_BIOMARKER', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_BIOMARKER_EEG', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_BIOMARKER_FNIRS', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_BIOMARKER_MRI', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_DIGITALHEALTH', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_DIGITALHEALTH_AUDIORESOURCE', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_DIGITALHEALTH_EYETRACKING', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_DIGITALHEALTH_VIDEORESOURCE', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_DIGITALHEALTH_VITALSIGNS', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_SELECTION', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_SELECTION_FIRST', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36'),
(6, 'TRIAL_SELECTION_SECOND', 'ecomylee', '2023-06-15 18:58:36', 'USER', '2023-06-15 09:58:36');


--
-- 테이블의 덤프 데이터 `T_RoleFunction`
--




-- 2022.06.22 -운영반영시점



INSERT INTO `T_RoleFunction` (`RoleSeq`, `FunctionId`, `AccessLevel`, `CUser`, `CDate`, `UUser`, `UDate`, `_Usage`, `_Version`) VALUES

(2, 'BIOMARKER', 105, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-13 10:31:21'),
(2, 'HUMAN_DERIVATIVE', 105, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-13 10:31:21'),
(2, 'SELECTION', 15, 'ecomylee', '2023-06-13 18:13:56', NULL, '2023-06-13 18:13:56', 'USER', '2023-06-13 09:13:56'),
(3, 'BIOMARKER', 11, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 07:44:51'),
(3, 'HUMAN_DERIVATIVE', 3, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 08:58:45'),
(3, 'IMAGING', 3, 'kapju', '2023-01-06 19:13:37', 'admin@BDP+ASD', '2023-06-15 17:55:21', 'USER', '2023-06-15 08:55:21'),
(3, 'SELECTION', 3, 'ecomylee', '2023-06-13 18:13:56', NULL, '2023-06-13 18:13:56', 'USER', '2023-06-15 08:58:45'),
(3, 'SETTING_PROJECT', 1, 'admin@BDP+ASD', '2023-06-09 16:00:46', 'admin@BDP+ASD', '2023-06-09 17:45:23', 'USER', '2023-06-15 06:14:58'),
(3, 'SUBJECT_MNG', 1, 'kapju', '2023-01-06 19:13:37', 'admin@BDP+ASD', '2023-06-15 17:55:29', 'USER', '2023-06-15 08:55:29'),
(4, 'BIOMARKER', 5, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 09:03:58'),
(4, 'HUMAN_DERIVATIVE', 5, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 09:03:58'),
(4, 'IMAGING', 5, 'kapju', '2023-01-06 19:13:37', 'admin@BDP+ASD', '2023-06-15 18:03:22', 'USER', '2023-06-15 09:03:22'),
(4, 'SELECTION', 5, 'ecomylee', '2023-06-13 18:13:56', NULL, '2023-06-13 18:13:56', 'USER', '2023-06-15 09:03:58'),
(4, 'SETTING_PROJECT', 0, 'admin@BDP+ASD', '2023-06-15 15:30:57', 'admin@BDP+ASD', '2023-06-15 16:29:04', 'USER', '2023-06-15 07:29:03'),
(4, 'SUBJECT_MNG', 1, 'kapju', '2023-01-06 19:13:37', 'admin@BDP+ASD', '2023-06-15 16:29:25', 'USER', '2023-06-15 07:29:24'),
(5, 'BIOMARKER', 7, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 08:49:33'),
(5, 'DIGITALHEALTH', 7, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 08:49:33'),
(5, 'HUMAN_DERIVATIVE', 7, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 08:49:33'),
(5, 'IMAGING', 7, 'kapju', '2023-01-06 19:13:37', 'admin@BDP+ASD', '2023-06-15 17:05:08', 'USER', '2023-06-15 08:05:08'),
(5, 'SELECTION', 7, 'ecomylee', '2023-06-13 18:13:56', NULL, '2023-06-13 18:13:56', 'USER', '2023-06-15 08:49:33'),
(6, 'BIOMARKER', 1, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 08:49:33'),
(6, 'DIGITALHEALTH', 1, 'kapju', '2023-03-20 19:06:10', NULL, '2023-03-20 19:06:10', 'USER', '2023-06-15 08:51:22'),
(6, 'HUMAN_DERIVATIVE', 1, 'ecomylee', '2023-06-13 15:57:12', NULL, '2023-06-13 15:57:12', 'USER', '2023-06-15 08:49:33'),
(6, 'SELECTION', 1, 'ecomylee', '2023-06-13 18:13:56', NULL, '2023-06-13 18:13:56', 'USER', '2023-06-15 08:49:33'),



UPDATE `T_RoleFunction` SET `RoleSeq` = 6,`FunctionId` = 'TRIAL_ALL',`AccessLevel` = 1,`CUser` = 'woonghee',`CDate` = '2023-04-13 15:15:45',`UUser` = NULL,`UDate` = '2023-04-13 15:15:45',`_Usage` = 'USER',`_Version` = '2023-06-15 08:51:22' WHERE `T_RoleFunction`.`RoleSeq` = 6 AND `T_RoleFunction`.`FunctionId` = 'TRIAL_ALL';

-- --------------------------------------------------------

-- `T_ServiceModuleFunction`
--2023.06.08

UPDATE `T_ServiceModuleFunction` SET `FunctionId` = 'BIOMARKER',`ModuleId` = 'TRIAL_DATA',`FunctionName` = '바이오마커',`Description` = NULL,`RStatus` = 'ACTIVE',`CDate` = '2023-06-08 11:43:01',`CUser` = 'eomylee',`UDate` = '2023-06-08 11:43:01',`UUser` = 'eomylee',`_Usage` = 'USER',`_Version` = '2023-06-08 02:43:01' WHERE `T_ServiceModuleFunction`.`FunctionId` = 'BIOMARKER';


-- 2023.06.08

UPDATE `T_ServiceModuleFunction` SET `FunctionId` = 'HUMAN_DERIVATIVE',`ModuleId` = 'TRIAL_DATA',`FunctionName` = '인체유래물',`Description` = NULL,`RStatus` = 'ACTIVE',`CDate` = '2023-06-08 11:45:00',`CUser` = 'eomylee',`UDate` = '2023-06-08 11:45:00',`UUser` = 'eomylee',`_Usage` = 'USER',`_Version` = '2023-06-08 02:45:00' WHERE `T_ServiceModuleFunction`.`FunctionId` = 'HUMAN_DERIVATIVE';

--2023.06.13

UPDATE `T_ServiceModuleFunction` SET `FunctionId` = 'SELECTION',`ModuleId` = 'TRIAL_DATA',`FunctionName` = '선별/임상',`Description` = NULL,`RStatus` = 'ACTIVE',`CDate` = '2023-06-13 17:06:05',`CUser` = 'eomylee',`UDate` = '2023-06-13 17:06:05',`UUser` = 'eomylee',`_Usage` = 'USER',`_Version` = '2023-06-13 09:13:27' WHERE `T_ServiceModuleFunction`.`FunctionId` = 'SELECTION';