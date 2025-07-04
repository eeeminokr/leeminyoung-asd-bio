<template>
  <div id="ContentsDiv">
    <div class="cont_body">
      <div class="box_stats" style="margin-top: 30px;">
        <div class="fl ">
          <h3 class="tit_bul" style="margin: 5px 0px 0px 20px;">전체 대상자 등록 현황</h3>
          <div id="chartdiv" style="width: 100%;height: 400px; left: 0px; margin: 20px 0px 30px 10px;"></div>
        </div>
        <div class="fr">
          <div class="tit_wrap">
            <h3 class="tit_bul" style="margin: 18px 0px 0px 20px;">참여병원 대상자 등록 현황</h3>
            <div class="tab_btns">
              <button type="button" v-for="(button, index) in buttons" :key="index"
                :class="{ 'active': selectedButton === index }" @click="selectButton(index)">{{ button }}</button>
            </div>
          </div>
          <div id="chartdiv2"
            style="width: 100%;height: 500px; font-size: 14px; font-family: 'NanumBarunGothic', 'dotum', 'Arial', 'Helvetica', 'sans-serif';">
          </div>
        </div>
      </div><!-- //.box_stats -->

      <div class="table_wrap" style="font-size: 20px; margin-top: 80px;">
        <h3 class="tit_bul">데이터 수집 현황</h3>
        <ul class="v_list">
          <li v-for="(button, index) in trialIndexs" :key="index" :class="{ 'active': selectedIndex === index }" @click="selectTrialIndexTab(index)">{{ button }}</li>
        </ul>
        <table class="tb_background" data-height="" v-if="dataLoaded === true">
          <tr>
            <td>
              <div class="scoll_head ps ps--active-x ps--active-y" v-on:scroll="onTableBodyScroll">
                <table class="tb_head">
                  <colgroup>
                    <col width="95px" />
                    <col width="69px" v-for="i in hospitals.length * 3" />
                  </colgroup>
                  <thead>
                    <tr>
                      <th rowspan="2" class="backslash"></th>
                      <th colspan="3" v-for="hospital in hospitals" :key="hospital">{{
                        `${orgIdToHospitalName[hospital]}`
                      }}</th>
                    </tr>
                    <tr class="normal">
                      <template v-for="hospital in hospitals" :key="hospital">
                        <th>대상자등록</th>
                        <th>수집 완료</th>
                        <th>미수집</th>
                      </template>
                    </tr>
                  </thead>
                </table>
              </div>
              <div class="scoll_body ps ps--active-x ps--active-y" style="max-height: 605px;"
                v-on:scroll="onTableBodyScroll">
                <table class="tb_body">
                  <colgroup>
                    <col width="95px" />
                    <col width="69px" v-for="i in hospitals.length * 3" />
                  </colgroup>
                  <tbody>
                    <tr v-for="label in labels" :key="label">
                      <td>{{ label }}</td>
                      <template v-for="hospital in hospitals" :key="hospital">
                        <td v-if="dataLoaded"
                          :class="getClass(orgIdStateList, label, hospital, 'totalRegisterStateCount')">
                          {{ getCount(orgIdStateList, label, hospital, 'totalRegisterStateCount') }}
                        </td>
                        <td v-else>-</td>
                        <td v-if="dataLoaded" :class="getClass(orgIdStateList, label, hospital, 'totalStateCount')">
                          {{ getCount(orgIdStateList, label, hospital, 'totalStateCount') }}
                        </td>
                        <td v-else>-</td>
                        <td v-if="dataLoaded" :class="getClass(orgIdStateList, label, hospital, 'totalNotStateCount')">
                          {{ getCount(orgIdStateList, label, hospital, 'totalNotStateCount') }}
                        </td>
                        <td v-else>-</td>
                      </template>
                    </tr>
                    <tr>
                      <td><strong>Total</strong></td>
                      <template v-for="hospital in hospitals" :key="hospital">
                        <td v-if="dataLoaded"><strong>{{ getTotal(hospital, 'totalRegisterStateCount') }}</strong></td>
                        <td v-else>-</td>
                        <td v-if="dataLoaded"><strong>{{ getTotal(hospital, 'totalStateCount') }}</strong></td>
                        <td v-else>-</td>
                        <td v-if="dataLoaded"><strong>{{ getTotal(hospital, 'totalNotStateCount') }}</strong></td>
                        <td v-else>-</td>
                      </template>
                    </tr>
                  </tbody>
                </table>
              </div>
            </td>
          </tr>
        </table>
        <table class="tb_background" data-height="" v-if="dataLoaded === false">
          <tr>
            <td>
              <div class="scoll_head ps ps--active-x ps--active-y" v-on:scroll="onTableBodyScroll">
                <table class="tb_head">
                  <colgroup>
                    <col width="95px" />
                    <col width="69px" v-for="i in hospitals.length * 3" />
                  </colgroup>
                  <thead>
                    <tr>
                      <th rowspan="2" class="backslash"></th>
                      <th colspan="3" v-for="hospital in hospitals" :key="hospital">{{
                        `${orgIdToHospitalName[hospital]}`
                      }}</th>
                    </tr>
                    <tr class="normal">
                      <template v-for="hospital in hospitals" :key="hospital">
                        <th>대상자등록</th>
                        <th>수집 완료</th>
                        <th>미수집</th>
                      </template>
                    </tr>
                  </thead>
                </table>
              </div>
              <div class="scoll_body ps ps--active-x ps--active-y" style="max-height: 605px;"
                v-on:scroll="onTableBodyScroll">
                <table class="tb_body">
                  <colgroup>
                    <col width="95px" />
                    <col width="69px" v-for="i in hospitals.length * 3" />
                  </colgroup>
                  <tbody>
                    <tr v-for="label in labels" :key="label">
                      <td>{{ label }}</td>
                      <template v-for="hospital in hospitals" :key="hospital">
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                      </template>
                    </tr>
                    <tr>
                      <td><strong>Total</strong></td>
                      <template v-for="hospital in hospitals" :key="hospital">
                        <td></td>
                        <td></td>
                        <td></td>
                      </template>
                    </tr>
                  </tbody>
                </table>
              </div>
            </td>
          </tr>
        </table>
        <div class="table_btm_wrap">
        <div class="table_btm">
          <table class="table" v-for="(label, index) in labels" :key="index">
            <colgroup>
              <col width="95px" />
              <col width="69px" />
              <col width="68px" />
            </colgroup>
            <tbody>
              <tr>
                <td rowspan="2" class="bg_g"><strong>{{ label }}</strong></td>
                <td>수집</td>
                <td class="bg_w">{{ summary[label].totalStateCount }}</td>
              </tr>
              <tr>
                <td>미수집</td>
                <td class="bg_w">{{ summary[label].totalNotStateCount }}</td>
              </tr>
            </tbody>
          </table>
          <table class="table">
            <colgroup>
              <col width="95px" />
              <col width="69px" />
              <col width="68px" />
            </colgroup>
            <tbody>
              <tr>
                <td rowspan="2" class="bg_g"><strong>Total</strong></td>
                <td>수집</td>
                <td class="bg_w">{{ total.totalStateCount }}</td>
              </tr>
              <tr>
                <td>미수집</td>
                <td class="bg_w">{{ total.totalNotStateCount }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
        <!-- //작업부분 -->
      </div>
      <!-- //#ContentsDiv -->
    </div>
  </div>
  <!-- //#bodycontainer -->

  <!-- //#wrap -->
</template>

<script>

import PerfectScrollbar from '../../assets/js/perfect-scrollbar'
import { jqueryUI } from '../../services/dashboard-ui';
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



const initScrollbar = function () {
  $('.scroll_body').each(function () {
    const ps = new PerfectScrollbar($(this)[0]);
  });
}

const tableBodyScrollFnc = function () {
  $('.scroll_body').scroll(function () {

    var xPoint = $('.scroll_body').scrollLeft();
    $('.scroll_head').scrollLeft(xPoint);
  });

}

export default {
  name: 'DashBoard',
  components: {
    PerfectScrollbar

  },

  props: {},

  data() {
    return {
      isActive: false,
      buttons: ['전체', '정상', '고위험', '자폐'], // 버튼 이름
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
      barchartData: [],
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
      tableTotalList: [{ zeroSeqList: [] }, { oneSeqList: [] }, { twoSeqList: [] }, { threeSeqList: [] }, { totalList: [] }],
      totalCountList: [],
      /*아래부터 데이터 수집현황 table data set변수*/
      dataLoaded: false,
      buttons: ['전체', '정상', '고위험', '자폐'], // 버튼 이름
      trialIndexs: ['V1', 'V2', 'V3', 'V4'],
      selectedIndex: 0,
      projectSeqStateList: [],
      orgIdToHospitalName: {
        10: '서울대병원',
        11: '연세세브란스',
        12: "분당서울대병원",
        13: "한양대병원",
        14: "은평성모병원",
        15: "서울아산병원",
        16: "충북대학교병원",
        17: "원광대병원"
      },
      hospitals: [10, 11, 12, 13, 14, 15, 16, 17],
      labels: ['정상', '고위험', '자폐', '보류'],
      orgIdStateList: [],
      summary: {
        정상: { totalStateCount: 0, totalNotStateCount: 0 },
        고위험: { totalStateCount: 0, totalNotStateCount: 0 },
        자폐: { totalStateCount: 0, totalNotStateCount: 0 },
        보류: { totalStateCount: 0, totalNotStateCount: 0 }
      },
      total: { totalStateCount: 0, totalNotStateCount: 0 }


    };
  },

  methods: {


    selectTrialIndexTab(index) {
      this.dataLoaded = false;
      initScrollbar();
      tableBodyScrollFnc();
      jqueryUI.perfectScrollbar.create();
      if (index !== this.selectedIndex) {
        this.selectedIndex = index;
      }
      this.getTrialInfoStateSurmmary(index + 1);

    },
    getClass(dataList, label, orgId, key) {
      const datas = dataList.find(item => item.label === label && item.orgId == orgId);
      if (key === 'totalRegisterStateCount') {
        return 'fc_color1';
      } else if (key === 'totalStateCount') {
        return 'fc_color2';
      }
    },
    getCount(dataList, label, orgId, key) {

      const data = dataList.find(item => item.label === label && item.orgId == orgId);
      return data ? data[key] : 0;
    },
    getTotal(orgId, key) {

      return this.labels.reduce((sum, label) => sum + this.getCount(this.orgIdStateList, label, orgId, key), 0);
    },
    getTrialInfoStateSurmmary(index) {
      DashboardService.getTrialInfoStateSurmmary(index).then(results => {
        if (results.data.succeeded) {
          const mergedValues = results.data.data;
          const orgIdDashBoard = mergedValues.orgIdDashBoard;
          this.orgIdStateList = [];
          this.summary = {
            정상: { totalStateCount: 0, totalNotStateCount: 0 },
            고위험: { totalStateCount: 0, totalNotStateCount: 0 },
            자폐: { totalStateCount: 0, totalNotStateCount: 0 },
            보류: { totalStateCount: 0, totalNotStateCount: 0 }
          };
          this.total = { totalStateCount: 0, totalNotStateCount: 0 };
          Object.keys(orgIdDashBoard).forEach(orgId => {
            const projectSeqMap = orgIdDashBoard[orgId];
            Object.keys(projectSeqMap).forEach(projectSeq => {
              const dashboard = projectSeqMap[projectSeq];
              let statusLabel;
              if (projectSeq == 1) {
                statusLabel = '정상';
              } else if (projectSeq == 2) {
                statusLabel = '고위험';
              } else if (projectSeq == 3) {
                statusLabel = '자폐';
              } else if (projectSeq == 4) {
                statusLabel = '보류';
              }
              this.orgIdStateList.push({
                orgId: orgId,
                label: statusLabel,
                totalRegisterStateCount: dashboard.totalRegisterStateCount || 0,
                totalStateCount: dashboard.totalStateCount || 0,
                totalNotStateCount: dashboard.totalNotStateCount || 0
              });
              this.summary[statusLabel].totalStateCount += dashboard.totalStateCount || 0;
              this.summary[statusLabel].totalNotStateCount += dashboard.totalNotStateCount || 0;
              this.total.totalStateCount += dashboard.totalStateCount || 0;
              this.total.totalNotStateCount += dashboard.totalNotStateCount || 0;
            });
          });
          this.dataLoaded = true;
          console.log("orgStateList : " ,this.orgIdStateList)
        }
      }).catch(error => {
        console.error("Error occurred:", error);
      }).finally(() => {
        this.dataLoaded = true;
        initScrollbar();
        tableBodyScrollFnc();
        jqueryUI.perfectScrollbar.create();
      });
    },

  /* chart logic 아래부터 */
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
        this.barchartData = [{
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
        }, {
          orgName: "서울아산병원",
          value: 0
        }, {
          orgName: "충북대학교병원",
          value: 0
        }, {
          orgName: "원광대병원",
          value: 0
        }
        ]
        results.data.data.forEach(item => {


          this.barchartData.filter((v, k) => {


            if (item != null) {
              if (this.barchartData[k].orgName === item.OrgName && id === item.ProjecSeq) {  //orgId 기관의 모든 건수 
                this.barchartData[k].value = parseInt(item.Count);
              } else if (this.barchartData[k].orgName === item.OrgName && item.ProjecSeq === undefined) {
                this.barchartData[k].value = parseInt(item.Count);
              }


            }

          })



        });

      }
      am5.array.each(am5.registry.rootElements, function (root) {
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




    let total = 0;
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

          if (it.ProjectSeq != 0 && it.ProjectSeq != 4) {
            this.tableTotalList[5].totalList[j] = (this.tableTotalList[5].totalList[j] || 0) + parseInt(it.Count);
            this.totalCountList[5] = (this.totalCountList[5] || 0) + parseInt(it.Count) //정상 고위험 자폐 총계 

          }

          for (let i = 0; i < maxVal + 1; i++) {
            if (it.ProjectSeq == i) {
              switch (i) {

                case 0:
                  this.tableTotalList[i].dropList[j] = it.Count
                  console.log("projectSeq0==>" + it.SubjectId)
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
  , pieChartProjectSeq(countData) {
    console.log("countData==> ", countData)
    am4core.options.commercialLicense = true;
    // am4core.addLicense("AM5C11203912101121"); 
    am4core.useTheme(am4themes_animated);


    let chart = am4core.create("chartdiv", am4charts.PieChart3D);
    am4core.options.minPolylineStep = 5;

    chart.events.on("sizechanged", function (ev) {
      chart.invalidateLayout();
    });


    chart.hiddenState.properties.opacity = 0; // this creates initial fade-in
    this.chartData.data.forEach((v, k) => {

      v.value = countData[k]






    })
    chart.data = this.chartData.data;
    const values = this.chartData.data.map(v => (v.value));
    chart.innerRadius = am4core.percent(40);
    chart.radius = am4core.percent(68);
    chart.depth = 70;
    chart.legend = new am4charts.Legend();



    chart.legend.labels.template.adapter.add("text", function (text, target) {
      let data = target.dataItem.dataContext;


      if (data && data.name && data.value) {
        return `${data.name}: (${data.value})명`;
      } else {
        return text;
      }
    });


    chart.legend.position = 'bottom'
    chart.legend.maxColumns = 3;

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


    series.bullets.push(function () {
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


  , onTableBodyScroll() {
    tableBodyScrollFnc();
  },
},
  mounted() {
    this.getProjectDataState();
    this.selectButton(this.selectedButton);
    this.selectTrialIndexTab(this.selectedIndex);
  },
  create() {

  }
}
</script>

<style scoped>
/* @import "../../assets/css/style";
@import  "../../assets/css/dashBoardstyle";
@import  "../../assets/css/perfect-scrollbar"; */
.sticky_last {
  right: "0px";
  border-left: "1px solid #c4c9ce";
}

#chartdiv {
  width: 100%;
  height: 500px;
}

.table_btm_wrap{
  width: 100%;
    position: relative;
    /* margin-top: 60px; */
}

</style>
