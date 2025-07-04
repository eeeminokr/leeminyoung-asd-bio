<template>
  <div id="ContentsDiv">
    <div class="cont_body">
      <div class="box_stats">
        <div class="fl ">
          <h3 class="tit_bul" style="margin: 5px 0px 0px 20px;">전체 대상자 등록 현황</h3>

          <div id="chartdiv" style="width: 100%;height: 400px; left: 0px; margin: 20px 0px 30px 10px;  "></div>

        </div>
        <div class="fr">
          <div class="tit_wrap">
            <h3 class="tit_bul" style="margin: 18px 0px 0px 20px;">참여병원 대상자 등록 현황</h3>
            <div class="tab_btns">
              <!-- <button  type="button"  v-bind:value="0" :class="{ 'active': isActive }"  @click="getDataSummarybyOrgId($event.target.value);">전체</button>
              <button type="button" v-bind:value="1" :class="{ 'active': isActive }"  @click="getDataSummarybyOrgId($event.target.value);">정상</button>
              <button type="button" v-bind:value="2" @click="getDataSummarybyOrgId($event.target.value)">고위험</button>
              <button type="button" v-bind:value="3" @click="getDataSummarybyOrgId($event.target.value)">자폐</button> -->
              <button type="button" v-for="(button, index) in buttons" :key="index" :class="{ 'active': selectedButton === index }" @click="selectButton(index)">{{ button }}</button>
            </div>
          </div>

          <div id="chartdiv2" style="width: 100%;height: 500px; font-size: 14px; font-family: 'NanumBarunGothic', 'dotum', 'Arial', 'Helvetica', 'sans-serif';"></div>
        </div>
      </div><!-- //.box_stats -->

      <div class="table_wrap" style="font-size: 20px;">
        <h3 class="tit_bul" style=" margin: 20px 0px 30px 10px;">데이터 수집 현황</h3>
        <table class="tb_background" data-height="">
          <tbody>
            <tr>
              <td>
                <div class="scroll_head">
          <table class="tb_head">
          <colgroup> 
            <col v-for="(monthdata, index) in monthList" :key="index"/>
            <col  /> 
          </colgroup>
          <thead>
            <tr>
              <th rowspan="2" class="backslash sticky"></th>
              <th v-for="(count, year) in colspanCount" :colspan="count">{{ year }}년</th>
              <th rowspan="2" class="sticky_last" style="right: 0px;">총계</th>
            </tr>
            <tr>
              <th v-for="(month, index) in data.month" :key="index">{{ month }}월</th>
            </tr> 
          </thead> 
          </table> <!--tb_head-->
          </div>
          <div class="scroll_body"  style=" position: relative; display: block; overflow-x: auto;">
            <!-- <perfect-scrollbar> -->
          <table class="tb_body" style=" width: 100%; ">
            <colgroup>
            <col/>
            <col v-for="(monthdata, index) in monthList" :key="index" />
            <col /> 
          </colgroup>
            <tbody>
            <tr v-for="(item, index) in tableTotalList" :key="index"  v-show="index != 0 && index !=4">
             <td class="sticky" style="left: 0px;"><span class="fw_bold" >{{  projectSeqs[index] }}</span></td>    
             <template v-for="(data, indexs) in item" :key="indexs"> <td v-for="value in data">{{ value }}</td></template>             
             <td class="sticky_last" style="right: 0px;"><span class="fw_bold fc_blue">{{ totalCountList[index] || 0 }}</span></td>
            </tr>         
          </tbody> 

          </table>
        <!-- </perfect-scrollbar> -->

        </div>
        




        </td>
        </tr>

          </tbody> <!--background 밑-->

        </table>










      </div>





    </div>
  </div>
</template>

<script>
import PerfectScrollbar from '../../assets/js/perfect-scrollbar';
import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts"
import am4themes_animated from "@amcharts/amcharts4/themes/animated";
import { ka, ta } from "date-fns/locale";
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';
import { findFont } from "@amcharts/amcharts4/.internal/core/utils/DOM";
import DashboardService from '../../services/dashboard.service';
import { tsImportEqualsDeclaration, tsTypeParameter } from "@babel/types";
import { utils } from '../../services/constants';
import { max } from 'date-fns';
import { isNotEmpty } from '@amcharts/amcharts4/.internal/core/utils/Utils';
// import * as $registry from "@amcharts/amcharts5/Registry";
// import { PerfectScrollbar } from 'vue3-perfect-scrollbar'
// import 'vue3-perfect-scrollbar/dist/vue3-perfect-scrollbar.css'
// require("../../assets/js/dashbaordtable-scrollbar.js")


	const initScrollbar = function() {
		$('.scroll_body').each(function(){
			const ps = new PerfectScrollbar($(this)[0]);
		});
	}

	const tableBodyScrollFnc = function() {
		$('.scroll_body').scroll(function () {

			var xPoint = $('.scroll_body').scrollLeft();
			$('.scroll_head').scrollLeft(xPoint);
		});

	}















export default {
  name: 'DashBoard2',
  components: {
    PerfectScrollbar
    
  },
 
  props: {

  },

  data() {
    return {
      isActive : false,
      buttons: ['전체','정상','고위험', '자폐'], // 버튼 이름
      selectedButton: 0,


      projects: [],
      trials: [],
      filters: {
        projectId: undefined,
        trialIndex: '',
      },
      chartData: {
        total: 0,
        data: [
          { name: "DROP", value: 0 },
          { name: "정상", value: 0 },
          { name: "고위험", value: 0 },
          { name: "자폐", value: 0 },
          { name: "보류", value: 0 },
        ],
      },
      barchartData: [ ],


         
      data: {
        year: [],
        month: [],
      },

      TableData: [],
      monthList: [],
      yearList: [],
      colspanCount: [], //colspan 월로계산한 값만큼 해당하는 년도 count colspan을 위한....
      countList: [],
      // projectSeq: ['정상','고위험','자폐'],

      projectSeq: [ 
          { name: "DROP", values: 0 },
          { name: "정상", values: 1 },
          { name: "고위험", values: 2 },
          { name: "자폐", values: 3 },
    ],
    
    projectSeqs: [ 
           "DROP", 
           "정상",
           "고위험",
           "자폐", 
       
    ],
          projectSeqList: [],
      tableTotalList: [ { zeroSeqList: []},{ oneSeqList: []},{ twoSeqList:  []},{ threeSeqList: []},{ totalList:[]} ],
      totalCountList: []
        };
  },

  computed: {

  },

  methods: {
    selectButton(index) {

      if (index !== this.selectedButton) {
        this.selectedButton = index;
      }
      this.getDataSummarybyOrgId(index);
    },
    getProjectDataState() {
      DashboardService.getProjectdDataState().then(results => {

        if (results.data.succeeded) {
          const dashboardData = [];
          const nullYearData = []; 
          results.data.data.forEach(item => {
  
            if (item.Year !== null) {
              this.TableData.push({
                ProjectSeq: item.ProjectSeq,
                SubjectId: item.SubjectId,
                Year: item.Year,
                Month: item.Month,
                Count: item.Count
              });
            } else {
              nullYearData.push(item); 
            }
          });


          let monthList = [];
          let countList = [];

          this.TableData.map((obj, idx) => {

            monthList.push(obj.Month)
            countList.push(obj.Count)

          })

          this.getDataTable(this.TableData);
        }
      });



    }
    ,
    getDataSummarybyOrgId(id) {
      DashboardService.getDataSummarybyOrgId(id).then(results => {
        this.isActive = true;
        let total = 0;
        if (results.data.succeeded) {
          const dashboardData = [];
       this.barchartData = [  {
        orgName: "서울대병원",
        value: 0
      }, {
        orgName: "연세세브란스",
        value: 0
      }, {
        orgName: "분당서울대병원",
        value: 0
      }, {
        orgName: "한양대병원",
        value: 0
      }, {
        orgName: "은평성모병원",
        value: 0
      },{
        orgName: "서울아산병원",
        value: 0
      },{
        orgName: "충북대학교병원",
        value: 0
      },{
        orgName: "원광대병원",
        value: 0
      }
      ] 
          results.data.data.forEach(item => {

 
             this.barchartData.filter((v,k) =>  {
            
              
                  if(item != null){
                    if(this.barchartData[k].orgName === item.OrgName && id === item.ProjecSeq ){  //orgId 기관의 모든 건수 
                      this.barchartData[k].value =  parseInt(item.Count);              
                    }else if(this.barchartData[k].orgName === item.OrgName && item.ProjecSeq === undefined ){
                      this.barchartData[k].value =  parseInt(item.Count);     
                    }


                }

              })

            

          });

        }
        am5.array.each(am5.registry.rootElements, function(root) {
      if (root.dom.id === "chartdiv2") {
        root.dispose();
        // root.container.children.clear(); 
      }
    });
        this.barChartOrgId(this.barchartData);
      });

      

    }

    , getDataTable(tabledata) {


      let startYear = Infinity;
      let startMonth = Infinity;
      // projectSeq 값을 담을 배열 선언
      const projectSeqs = [];
    
      tabledata.forEach(({ Year, Month, ProjectSeq, Count }) => {
        const year = Number(Year);
        const month = Number(Month);
        const count = Number(Count);
        const projectSeq = ProjectSeq;


        // projectSeq 값을 배열에 추가
        if (!projectSeqs.includes(projectSeq)) {
          projectSeqs.push(projectSeq);
        }

        if (year < startYear) {
          startYear = year;
          startMonth = month;
        } else if (year === startYear && month < startMonth) {
          startMonth = month;
        }
      });

      console.log(`Smallest Year: ${startYear}, Smallest Month: ${startMonth}`);





      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;
      const passedMonths = (startYear < currentYear) ? 12 : currentMonth - startMonth + 1;
      const nextYearMonthsToPrint = (currentYear === startYear) ? 12 - startMonth + 1 : currentMonth;
      const nextYearMonths = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];



      for (let year = startYear; year <= currentYear + 1; year++) {

        for (let month = 1; month <= 12; month++) {

          if (year === startYear && month < startMonth) {
            continue;
          }

          if (year === currentYear + 1 && month > nextYearMonthsToPrint) {
            continue;
          }

          const monthData =
            (year === currentYear && month === currentMonth) ? currentMonth : nextYearMonths[month - 1];

          if (year === startYear && month <= startMonth + passedMonths - 1) {
            this.data.year.push(year);
            this.data.month.push(monthData);
          }

          if (year > startYear || (year === startYear && month > startMonth + passedMonths - 1)) {
            this.data.year.push(year);
            this.data.month.push(monthData);
          }
        }
      }






      this.colspanCount = {};
    this.data.year.forEach(year => {
      if (this.colspanCount[year]) {
        this.colspanCount[year] += 1;
      } else {
        this.colspanCount[year] = 1;
      }
    });





      this.tableTotalList = [
      { dropList: Array(this.data.month.length).fill(0) },
  { oneSeqList: Array(this.data.month.length).fill(0) },
  { twoSeqList: Array(this.data.month.length).fill(0) },
  { threeSeqList: Array(this.data.month.length).fill(0) },
  { fourSeqList: Array(this.data.month.length).fill(0) },
  { totalList: Array(this.data.month.length).fill(0) },
];




let total =0;
this.totalCountList = Array(4)
// 배열에서 ProjectSeq 속성이 존재하는 값을 필터링한 후, 그 중에서 최대값을 구함
const filteredArr = tabledata.filter(v => v.ProjectSeq && !isNaN(v.ProjectSeq));
const minVal = Math.min(...filteredArr.map(v => v.ProjectSeq));
const maxVal = Math.max(...filteredArr.map(v => v.ProjectSeq));

tabledata.filter(it => {

  for (let j = 0; j < this.data.month.length; j++) {
    if (it.Month == this.data.month[j] && it.Year == this.data.year[j]) {
      //  total += parseInt(it.Count)
      // this.tableTotalList[6].totalList[j] 
      // this.tableTotalList[6].totalList[j]  = total;

      if( it.ProjectSeq != 0  && it.ProjectSeq != 4){
      this.tableTotalList[5].totalList[j] = (this.tableTotalList[5].totalList[j] || 0) + parseInt(it.Count); 
      this.totalCountList[5] = (this.totalCountList[5] || 0) + parseInt(it.Count) //정상 고위험 자폐 총계 
     
    }

      for (let i = 0; i < maxVal+1; i++) {
        if (it.ProjectSeq == i ) {
          switch (i) {
           
            case 0:
               this.tableTotalList[i].dropList[j] = it.Count
                console.log("projectSeq0==>"+it.SubjectId)
               this.totalCountList[i] = (this.totalCountList[i] || 0) + parseInt(it.Count); 
              break;
           
              case 1:
              this.tableTotalList[i].oneSeqList[j] = it.Count
              this.totalCountList[i] = (this.totalCountList[i] || 0) + parseInt(it.Count); 
       
              break;
            case 2:
              this.tableTotalList[i].twoSeqList[j] = it.Count
              this.totalCountList[i] = (this.totalCountList[i] || 0) + parseInt(it.Count); 
              break;
            case 3:
              this.tableTotalList[i].threeSeqList[j] = it.Count
              this.totalCountList[i] = (this.totalCountList[i] || 0) + parseInt(it.Count); 
              break;
             case 4:
               this.tableTotalList[i].fourSeqList[j] = it.Count
               this.totalCountList[i] = (this.totalCountList[i] || 0) + parseInt(it.Count); 
               break;
     

          }
    
        }


      }
    }
  }
});

  this.pieChartProjectSeq(this.totalCountList)
  
    }
    ,pieChartProjectSeq(countData) {
      console.log("countData==> ",countData)
      am4core.options.commercialLicense = true;
      // am4core.addLicense("AM5C11203912101121"); 
      am4core.useTheme(am4themes_animated);

      
      let chart = am4core.create("chartdiv", am4charts.PieChart3D);
        am4core.options.minPolylineStep =5;

        chart.events.on("sizechanged", function(ev) {
          chart.invalidateLayout();
        });  


      chart.hiddenState.properties.opacity = 0; // this creates initial fade-in
      this.chartData.data.forEach((v,k) =>{
      
             v.value = countData[k]
       

     



})
      chart.data = this.chartData.data;
      const values =  this.chartData.data.map(v => (v.value));
      chart.innerRadius = am4core.percent(40);
      chart.radius = am4core.percent(68);
      chart.depth = 70; 
      chart.legend = new am4charts.Legend();
      


      chart.legend.labels.template.adapter.add("text", function(text, target) {
  let data = target.dataItem.dataContext;


  if (data && data.name && data.value) { 
    return `${data.name}: (${data.value})명`;
  } else {
    return text;
  }
});


      chart.legend.position = 'bottom'
      chart.legend.maxColumns =3;

      chart.maxLeft = 200
     
      let series = chart.series.push(new am4charts.PieSeries3D());

  
      series.slices.template.propertyFields.fill = "color";
      series.dataFields.value = "value";

      series.dataFields.depthValue = "value";
      series.dataFields.category = "name";
      series.slices.template.cornerRadius = 5;

      series.colors.step = 3;

      series.hiddenState.properties.opacity = 1;
      series.hiddenState.properties.endAngle = -90;

      series.hiddenState.properties.startAngle = -90;
      chart.hiddenState.properties.radius = am4core.percent(0);
      chart.fontSize = 14
      chart.fontFamily = 'NanumBarunGothic', 'dotum', 'Arial', 'Helvetica', 'sans-serif';





    },

    barChartOrgId(barData) {
  
 //  am5.addLicense("AM5C11203912101121");
      var root = am5.Root.new("chartdiv2"); //Root 요소 생성

 

      var myTheme = am5.Theme.new(root);
      
      
      myTheme.rule("Grid", ["base"]).setAll({
        strokeOpacity: 0.1

      });

      root.setThemes([
        am5themes_Animated.new(root),
        myTheme
      ]);


      var chart = root.container.children.push(
        am5xy.XYChart.new(root, {      
          panX: true,
          panY: true,
          wheelX: "zoomX",
          wheelY: "panX",
          pinchZoomX: true,
          

        })
      );
      

      var cursor = chart.set("cursor", am5xy.XYCursor.new(root, {}));
          cursor.lineX.set("visible", false);

      var yRenderer = am5xy.AxisRendererY.new(root, {   //축 렌더러 기본클래스 부모는
        minGridDistance: 30 //그리드 선 사이의 최소 거리(픽셀 단위)
      });
      yRenderer.grid.template.set("location", 1); // default :1 값


      var yAxis = chart.yAxes.push(am5xy.CategoryAxis.new(root, {
        maxDeviation: 0,
        categoryField: "orgName",
        renderer: yRenderer,
        tooltip: am5.Tooltip.new(root, {})
      }));

      var xAxis = chart.xAxes.push(am5xy.ValueAxis.new(root, {
        maxDeviation: 0,
        min: 0,
        renderer: am5xy.AxisRendererX.new(root, {
          visible: false,
          strokeOpacity: 0.1
        })
      }));



      var series = chart.series.push(
        am5xy.ColumnSeries.new(root, {
          name: "Series 1",
          xAxis: xAxis,
          yAxis: yAxis,
          valueXField: "value",
          sequencedInterpolation: true,
          categoryYField: "orgName",
          tooltip: am5.Tooltip.new(root, {
            labelText: "{valueX}",
            
          
          }),
        })
      );

    
  //     series.columns.template.setAll({
  //   height: am5.p100,
  //   strokeOpacity: 0
  // });


  series.bullets.push(function() {
    return am5.Bullet.new(root, {
      locationX: 1, // 데이터 항목의 위치 설정 속성
      locationY: 0.5,
      sprite: am5.Label.new(root, {
        centerY: am5.p50,
        text: "({valueX})명",
        populateText: true
      })
    });
  });

      var columnTemplate = series.columns.template;

      columnTemplate.setAll({  //컬럼 그래프 코너 부분 
        draggable: true,
        cursorOverStyle: "pointer",
        cornerRadiusBR: 5,
        cornerRadiusTR: 5,
        strokeOpacity: 0,
      
      });
      // columnTemplate.fontSize = 10;

      columnTemplate.adapters.add("fill", (fill, target) => { //adapters는 요소 설정 값으 동적으로 변경하는데 사용
        return chart.get("colors").getIndex(series.columns.indexOf(target));
      });

      columnTemplate.adapters.add("stroke", (stroke, target) => {
        return chart.get("colors").getIndex(series.columns.indexOf(target));
      });
   

      columnTemplate.events.on("dragstop", () => {  //다양한 사용자 상호 작용(클릭, 호버 등)에 대한 이벤트 핸들러를 요소에 연결하기 위해 속성을 통해 액세스할 수 있는 이벤트 디스패처를 사용
        sortCategoryAxis();               //요소 끌기가 중지될 때 호출
      });

      // Get series item by category
      function getSeriesItem(category) {
        for (var i = 0; i < series.dataItems.length; i++) {
          var dataItem = series.dataItems[i];
          if (dataItem.get("categoryY") == category) {
            return dataItem;
          }
        }
      }


      // Axis sorting
      function sortCategoryAxis() {
        // Sort by value
        series.dataItems.sort(function (x, y) {  //[dataItems]:구성 요소의 데이터 항목 목록
          return y.get("graphics").y() - x.get("graphics").y();
        });

        var easing = am5.ease.out(am5.ease.cubic);

        // Go through each axis item
        am5.array.each(yAxis.dataItems, function (dataItem) {
          // get corresponding series item
          var seriesDataItem = getSeriesItem(dataItem.get("category"));

          if (seriesDataItem) {
            // get index of series data item
            var index = series.dataItems.indexOf(seriesDataItem);

            var column = seriesDataItem.get("graphics");

            // position after sorting
            var fy =
              yRenderer.positionToCoordinate(yAxis.indexToPosition(index)) -
              column.height() / 2;


            // set index to be the same as series data item index
            if (index != dataItem.get("index")) {
              dataItem.set("index", index);

              // current position
              var x = column.x();
              var y = column.y();

              column.set("dy", -(fy - y));
              column.set("dx", x);

              column.animate({
                key: "dy",
                to: 0,
                duration: 600,
                easing: easing
              });
              column.animate({
                key: "dx",
                to: 0,
                duration: 600,
                easing: easing
              });
            } else {
              column.animate({
                key: "y",
                to: fy,
                duration: 600,
                easing: easing
              });
              column.animate({
                key: "x",
                to: 0,
                duration: 600,
                easing: easing
              });
            }
          }
        });

        // Sort axis items by index.
        // This changes the order instantly, but as dx and dy is set and animated,
        // they keep in the same places and then animate to true positions.
        yAxis.dataItems.sort(function (x, y) {
          return x.get("index") - y.get("index");
        });
      }

      yAxis.data.setAll(barData);
      series.data.setAll(barData);


      // Make stuff animate on load
      // https://www.amcharts.com/docs/v5/concepts/animations/
      series.appear(1000);
      chart.appear(1000, 100);

    }


    ,onTableBodyScroll() {
				tableBodyScrollFnc();
			},


  },
  mounted() {
    this.getProjectDataState();
    this.selectButton(this.selectedButton);
  },
  created() {

  }
}





</script>

<style scoped>

  .sticky_last {
    right : "0px";
    border-left: "1px solid #c4c9ce";

  }

  #chartdiv {
  width: 100%;
  height: 500px;
}
</style>