<template>
		
<div id="wrap">


</div>
<!-- //#wrap -->




<!-- 팝업 -->
<div class="popup_bg"></div>
<div class="pop_policy">
	<button type="button" class="btn_close" @click="closeModal"><span class="blind">팝업 창닫기</span></button>
	<h3 class="pop_head">아이AI 개인정보처리방침</h3>
	<div class="pop_cont">
		<p>아이AI 프로젝트 참여기관은 개인정보보호법에 따라 대상자의 개인정보 및 권익을 보호하고 개인정보와 관련한 데이터를 원활하게 처리할 수 있도록 하기 위하여 다음과 같은 처리방침을 두고 있습니다</p>
		<ul class="terms_list">
			<li>
				<h4>제1조(개인정보의 수집목적)</h4>
				<p>아이AI 프로젝트 참여기관은 다음의 목적을 위하여 개인정보를 수집합니다. 수집하고 있는 개인정보는 다음의 목적 이외의 용도로는 이용되지 않으며,<br> 이용 목적이 변경되는 경우에는 개인정보 보호법 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행할 예정입니다.</p>
				<p>- 수집목적 : 개인정보 수집을 통한 대상자 등록 및 수집 데이터 관리</p>
				<p>- 수집방법 : 과제 참여 신청을 통해 기입한 정보를 웹페이지에 등록하는 방식</p>
			</li>
			<li>
				<h4>제2조(개인정보의 처리 및 보유기간)</h4>
				<p>아이AI 프로젝트 참여기관은 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.</p>
				<p>- 보유기간 : 10년</p>
			</li>
			<li>
				<h4>제3조(개인정보의 제공)</h4>
				<p>아이AI 프로젝트 참여기관은 원칙적으로 정보주체의 개인정보를 수집·이용 목적으로 명시한 범위 내에서 처리하며, 다음의 경우를 제외하고는 정보주체의 사전 동의 없이는 본래의 목적 범위를 초과하여 처리하거나 제3자에게 제공하지 않습니다.</p>
				<p>- 정보주체로부터 별도의 동의를 받은 경우</p>
				<p>- 학술연구 등의 목적을 위하여 필요한 경우로서 특정 개인을 알아볼 수 없는 형태로 개인정보를 제공하는 경우</p>
			</li>
			<li>
				<h4>제4조(처리하는 개인정보 항목)</h4>
				<p>아이AI 프로젝트 참여기관은 다음의 개인정보 항목을 처리하고 있습니다.</p>
				<p>- 대상자 ID, 대상자 성별, 대상자 생년월일, 보호자 연락처, 서면동의일</p>
			</li>
			<li>
				<h4>제5조(개인정보의 파기)</h4>
				<p>아이AI 프로젝트 참여기관은 개인정보 보유기간의 경과, 처리목적 달성 등 개인정보가 불필요하게 되었을 때에는 지체없이 해당 개인정보를 파기합니다. 개인정보 파기의 절차 및 방법은 다음과 같습니다.</p>
				<p>- 아이AI 프로젝트 참여기관은 파기하여야 하는 개인정보(또는 개인정보파일)에 대해 개인정보 파기계획을 수립하여 파기합니다. 아이AI 프로젝트 참여기관은 파기 사유가 발생한 개인정보(또는 개인정보파일)를 선정하고, 아이AI 프로젝트 주관기관장의 승인을 받아 개인정보(또는 개인정보파일)를 파기합니다.</p>
				<p>- 아이AI 프로젝트 참여기관은 전자적 파일 형태로 기록·저장된 개인정보는 기록을 재생할 수 없도록 로우레밸포멧(low level format) 등의 방법을 이용하여 파기하며, 종이 문서에 기록·저장된 개인정보는 분쇄기로 분쇄하거나 소각하여 파기합니다.</p>
			</li>
			<li>
				<h4>제6조(개인정보의 안전성 확보조치)</h4>
				<p>아이AI 프로젝트 참여기관은 개인정보의 안전성 확보를 위해 다음과 같은 조치를 취하고 있습니다.</p>
				<p> - 개인정보를 취급하는 직원은 반드시 필요한 인원에 한하여 지정·관리하고 있으며 취급직원을 대상으로 안전한 관리를 위한 교육을 실시하고 있습니다.</p>
				<p>- 아이AI 프로젝트 참여기관은 전자적 파일 형태로 기록·저장된 개인정보는 기록을 재생할 수 없도록 로우레밸포멧(low level format) 등의 방법을 이용하여 파기하며, 종이 문서에 기록·저장된 개인정보는 분쇄기로 분쇄하거나 소각하여 파기합니다.</p>
				<p>- 개인정보를 처리하는 데이터베이스시스템에 대한 접근권한의 부여·변경·말소를 통하여 개인정보에 대한 접근통제를 위한 필요한 조치를 하고 있으며 침입차단시스템을 이용하여 외부로부터의 무단 접근을 통제하고 있습니다.</p>
				<p> - 개인정보처리시스템에 접속한 기록(웹 로그, 요약정보 등)을 보관·관리하고 있습니다.</p>
				<p>- 개인정보는 암호화 등을 통해 안전하게 저장 및 관리되고 있습니다. 또한 중요한 데이터는 저장 및 전송 시 암호화하여 사용하는 등의 별도 보안기능을 사용하고 있습니다.</p>
				<p>- 해킹이나 컴퓨터 바이러스 등에 의한 개인정보 유출 및 훼손을 막기 위하여 보안프로그램을 설치하고 주기적으로 갱신·점검하고 있습니다.</p>
				<p> - 개인정보를 보관하고 있는 개인정보시스템의 물리적 보관 장소를 네이버 공공Cloud에 두고 운영하고 있습니다.</p>
			</li>
			<li>
				<h4> 제7조(개인정보 보호책임자)</h4>
				<p>아이AI 프로젝트 참여기관은 개인정보 보유기간의 경과, 처리목적 달성 등 개인정보가 불필요하게 되었을 때에는 지체없이 해당 개인정보를 파기합니다. 개인정보 파기의 절차 및 방법은 다음과 같습니다.</p>
				<p>아이AI 프로젝트 참여기관은 개인정보 처리에 관한 업무를 총괄해서 책임지고, 개인정보 처리와 관련한 정보주체의 불만처리 및 피해구제 등을 위하여 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.</p>
			</li>
			<li>
				<h4>개인정보 보호책임자</h4>
				<p>성 명 : 김상원</p>
				<p>직 책 : 차장</p>
				<p>연락처 : 02-6412-2403, won.kim@ecoinsight.co.kr</p>
				<p>아이AI 프로젝트 참여기관은 개인정보처리방침을 개정하는 경우 AI플랫폼을 통하여 공지할 것입니다.</p>
			</li>
			<li>
				<h4>개인정보처리방침 변경</h4>
				<p>이 개인정보처리방침은 2023. 03. 20부터 적용됩니다.</p>
				<p>이전의 개인정보처리방침은 아래에서 확인할 수 있습니다.</p>
				<p>◎ 2023. 03. 20 적용지침</p>
			</li>
		</ul>
	</div>
</div>
<!-- //팝업 -->



</template>
<script>
export default {
	name: 'PolicyModal',
	components: {
	},
	data() {
		return {
		}
	},
	emits: ['onClose'],
	computed: {
	},
	methods: {
		closeModal(){
			this.$emit('onClose', false);
		}

	},
	mounted() {
	},
	created() {
	}
}
</script>

<style scoped>











 /* 개인정보처리방침 : 팝업 */
 	.popup_bg {position:fixed;top:0;left:0;z-index:1000;width:100%;height:100%;background:rgba(0 0 0 / 50%);}
    .pop_policy {position:fixed;top:90px;left:50%;z-index:1001;max-width:1000px;width:100%;height:calc(100% - 238px);transform:translateX(-50%);}
    .pop_policy .btn_close {position:absolute;top:0;right:0;width:78px;height:78px;background-color:transparent;cursor:pointer;}
    .pop_policy .btn_close:before, .pop_policy .btn_close:after {content:"";position:absolute;top:37px;left:27px;display:block;width:24px;height:2px;background-color:#fff;}
    .pop_policy .btn_close:before {transform:rotate(-45deg);}
    .pop_policy .btn_close:after {transform:rotate(45deg);}
    .pop_policy .pop_head {padding:26px 10px 22px;line-height:30px;font-size:26px;font-weight:700;color:#fff;text-align:center;background-color:#0a6ea7;border-radius:13px 13px 0 0;}
    .pop_policy .pop_cont {overflow:auto;padding:40px 50px;max-height:845px;height:calc(100% - 78px);background-color:#fff;border-radius:0 0 13px 13px;box-sizing:border-box;}
    .pop_policy .pop_cont p {line-height:20px;font-size:14px;color:#333;}
    .pop_policy .pop_cont .terms_list {margin-bottom:30px;}
    .pop_policy .pop_cont .terms_list li {margin-top:30px;}
    .pop_policy .pop_cont .terms_list li h4 {line-height:25px;font-size:18px;font-weight:700;color:#333;}
    .pop_policy .pop_cont .terms_list li p {margin-top:6px;}
    .pop_policy .pop_cont::-webkit-scrollbar {width:17px;background:transparent;}
    .pop_policy .pop_cont::-webkit-scrollbar-track {background:transparent;}
    .pop_policy .pop_cont::-webkit-scrollbar-thumb {border:5px solid transparent;border-radius:17px;background:#a4a9ac;background-clip:padding-box;}



	






</style>
