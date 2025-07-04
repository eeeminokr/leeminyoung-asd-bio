$(document).ready(function(){

	// GNB
	$('#gnb').find('.menu > li.current').find('.submenu').show();
	$('#gnb').find('.menu > li > a').click(function(){
		var hasSubmenu = $(this).parent().find('.submenu').length > 0;
		if(hasSubmenu === true) {
			$(this).parent().siblings().removeClass('current').find('.submenu').slideUp();
			$(this).parent().addClass('current').find('.submenu').slideDown();

			return false;
		}
	});

	// table scroll - 스크롤 생성
	$(".tb_background").each(function(){
		var table = $(this);
		var tableColgroup = table.find("colgroup").html();
		var tableThead = table.find("thead").html();
		var tableTbody = table.find("tbody").html();
		var tbodyHeight = table.attr('data-height') + 'px';

		var setHTML = '<tr><td><div class="scroll_head"><table class="tb_head"><colgroup>' + tableColgroup + '</colgroup><thead>' + tableThead + '</thead></table></div><div class="scroll_body"><table class="tb_body"><colgroup>' + tableColgroup + '</colgroup><tbody>' + tableTbody + '</tbody></table></div></td></tr>';

		table.html(setHTML);
		table.find(".scroll_body").css('max-height', tbodyHeight);
	});

	// table scroll - 가로 스크롤 움직임
	$('.scroll_body').scroll(function () {
		var xPoint = $(this).scrollLeft();
		$(this).closest('.tb_background').find('.scroll_head').scrollLeft(xPoint);
	});

	// 테이블 스크롤 스타일 플러그인 추가함
    $('.scroll_body').each(function(){
		const ps = new PerfectScrollbar($(this)[0]);
	});
		

	// input[type="file"] 스타일 변경을 위한 스크립트
	var fileTarget = $('.filebox .upload_hidden');
	fileTarget.on('change', function(){
		if(window.FileReader){ 
			// modern browser 
			var filename = $(this)[0].files[0].name; 
		} else { 
			// old IE 
			var filename = $(this).val().split('/').pop().split('\\').pop();
		} 
		$(this).siblings('.upload_name').val(filename); 
	});

	// 달력
	$('.datepicker').datepicker({ dateFormat: 'yy-mm-dd' });

	// 테이블 체크박스 All
	$('table').each(function(){
		var $prTable = $(this);
		var $checkAll = $prTable.find('.check_all');
		$checkAll.change(function () {
			var $this = $(this);
			var checked = $this.prop('checked');
			$prTable.find('input[name="table_check"]').prop('checked', checked);
		});
	
		var boxes = $prTable.find('input[name="table_check"]');
		boxes.change(function () {
			var boxLength = boxes.length;
			var checkedLength = $prTable.find('input[name="table_check"]:checked').length;
			var selectAll = (boxLength == checkedLength);
			$checkAll.prop('checked', selectAll);
		});
	});

	// 권한설정 데이터 OX
	$('.btn_ox').click(function(){
		var oxClild = $(this).text() == 'O';
		if(oxClild == true) {
			$(this).text('X');
		} else {
			$(this).text('O');
		}
	});

	// dropdown
	$('.dropdown_box').each(function(){
		var select = $(this);
		var sTitle = select.find('.tit');
		sTitle.click(function(){
			if(select.hasClass('open') == true){
				select.removeClass('open');	
			} else {
				select.addClass('open');
				select.siblings().removeClass('open');
			}
		});

		var checkLength = $(this).find('.pop').find('input:checkbox[name]:checked').length;
		if(checkLength == 0) {
			$(this).removeClass('active');
		} else {
			$(this).addClass('active');
		}

		$(this).find('.pop').find('input:checkbox').click(function(){
			var popCheckLength = $(this).closest('ul').find('input:checked').length;
			if( popCheckLength == 0 ){
				$(this).closest('.dropdown_box').removeClass('active');
			} else {
				$(this).closest('.dropdown_box').addClass('active');
			}
		});
	});
	$(document).mouseup(function(e){
		if( $(".dropdown_box").has(e.target).length === 0 ){
			$(".dropdown_box").removeClass('open');
		}
	});


	// 그룹 select style - dropdown box
	$('.selectbox').each(function(){
		$(this).find('.select_default').click(function(){
			if($(this).siblings('.select_dropdown').css('display','none') == false) {
				$(this).removeClass('on');
				$(this).siblings('.select_dropdown').hide();
			} else {
				$('.select_default').removeClass('on');
				$('.select_dropdown').hide();
				$(this).addClass('on');
				$(this).siblings('.select_dropdown').show();
			}
		});
		$(this).find('.option').click(function(){
			var thsVal = $(this).attr('data-value');
			$(this).parent('.select_dropdown').hide();
			$(this).closest('.selectbox').find('.select_default').html(thsVal);
			if(thsVal == 'Custom'){
				var btn1 = $(this).closest('td').next().find('.btn');
				btn1.attr('onclick','prem_pop2()').addClass('btn_skyblue').text('권한설정');
			} else {
				var btn2 = $(this).closest('td').next().find('.btn');
				btn2.attr('onclick','prem_pop()').removeClass('btn_skyblue').text('권한보기');
			}
		});
	});
	$(document).mouseup(function(e){
		if( $(".selectbox").has(e.target).length === 0 ){
			$(".selectbox").removeClass('on')
			$('.select_dropdown').hide();
		}
	});

	

});


//계정정보 팝업
function user_pop(){
	$('.user_pop').toggle('fade');

	// 계정정보팝업 닫기
	$(document).mouseup(function(e){
		if( $(".utility").has(e.target).length === 0 ){
			$(".user_pop").fadeOut();
		}
	});
}
// 사용자 정보변경 팝업
function pop_userinfo(){
	$('.pop_userinfo').bPopup();
}
// 비밀번호 변경 팝업
function pop_changepw(){
	$('.pop_changepw').bPopup();
}