$(document).ready(function(){

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

    // 테이블 스크롤시 고정되는 부분이 있는 경우
    $("tr").each(function(){
        var sum = 0;
        $(this).find(".sticky").each(function(){
            if( $(this).prev().length == 0 ){
                $(this).css("left", "0px");
            } else {
                var leftNum = $(this).prev().outerWidth();
                sum += leftNum;
                $(this).css("left", sum + "px");
            }
        });

        $(this).find(".sticky_last").each(function(){
            $(this).css({
                "right" : "0px",
                "border-left" : "1px solid #c4c9ce"
            });
            $(this).prev().css("border-right","0px");
        });
    });

});