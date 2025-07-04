import dayjs from 'dayjs';

const QC_ICON_NO_SUBJECT = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyRpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDYuMC1jMDA2IDc5LmRhYmFjYmIsIDIwMjEvMDQvMTQtMDA6Mzk6NDQgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCAyMi40IChXaW5kb3dzKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDoxRUI3MkVFM0Y1RUIxMUVCOTc3OTk4M0VGQzNGQ0Q0RSIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDoxRUI3MkVFNEY1RUIxMUVCOTc3OTk4M0VGQzNGQ0Q0RSI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjFFQjcyRUUxRjVFQjExRUI5Nzc5OTgzRUZDM0ZDRDRFIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjFFQjcyRUUyRjVFQjExRUI5Nzc5OTgzRUZDM0ZDRDRFIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+HUjo9gAAATJJREFUeNqkVKFyg0AQ3RIQ4BgcnwASpg5kVTlfFNMvAN3I1vEBVZH4JlWV4DIYBAytw/EDgKS7TKjo5JpC3gxi9/bt7Tvu3Y1lWXCCZ9v2vqoq6PseeFAUBQzDgDzPPQzfKSee1p5d133yfX8qoEIeaAPaSJblQ5ZlL5jaAk5yH4bhuAZRFI3I9zaMsc8gCEDXdVgKTdOgbdsHcBxn7Lpu1STEI75AGs+dwePH7fTx4vmQiS9eGvk38RwE3sLu7viv3J9NlkBYIoMn7eIkJIEn4wd4WcZrQHxh/k1rMF8PgbxCXlgD4hFfRDcy7LZHBy9ukiQJuZlt0DNfTdNIdV275AVVVUGSJC5xGAYoigLiOIY0TcnFr/ON3WLiiBrfyrKcCnnAJwBM05wmwPBAuW8BBgDZQyP0WUDDRwAAAABJRU5ErkJggg==";
const QC_ICON_NO_DATA = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyRpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDYuMC1jMDA2IDc5LmRhYmFjYmIsIDIwMjEvMDQvMTQtMDA6Mzk6NDQgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCAyMi40IChXaW5kb3dzKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDpDRDExOTU3RUY1RUIxMUVCQjY1QkMzN0ZFMjk2NzBDQyIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDpDRDExOTU3RkY1RUIxMUVCQjY1QkMzN0ZFMjk2NzBDQyI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOkNEMTE5NTdDRjVFQjExRUJCNjVCQzM3RkUyOTY3MENDIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOkNEMTE5NTdERjVFQjExRUJCNjVCQzM3RkUyOTY3MENDIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+wbpyHAAAASxJREFUeNqclM1OwlAQhef+UAiIT4MsfQL1EVy6cqEkutElbDTBjYmJS18B30BWYnwZMGLbSz2nSNJYbn+YZHKb6Zxvzm0yVUmSCOPufnyM4xJ5gNwTfyyQM+T4+mrwyoIiBIChaQQ3NmiJMQ1UlR+BfuciiX+W4uJwBNCtaXf2j4wNnoJ2V7SxxYB0rBKtjWCoJCt3OH2bfmqUB7bZwrsScY6lhM75CQjpaV5hh/jT9QjpVnVxfnaaZtYN9brq1Kw456jKxOzz4/NLrt/Wmb4N4HWyrdkH8EL+i4oApdcpE2edzDf7UzvWugUhHyvswi7h1roZIQ9xuJS6bthPHfUaWzhxUTgKv+fcyo3F4i1GX9ofpVs8UZn/yQmOC2Qf2SnAfCHf6YAAFn4FGAAbuHt5n7EFmQAAAABJRU5ErkJggg==";
const QC_ICON_NO_QC = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyRpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDYuMC1jMDA2IDc5LmRhYmFjYmIsIDIwMjEvMDQvMTQtMDA6Mzk6NDQgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCAyMi40IChXaW5kb3dzKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDpFMDFBQUMyMkY1RUIxMUVCQUYyOUM5RkQwNjA3RjNEOCIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDpFMDFBQUMyM0Y1RUIxMUVCQUYyOUM5RkQwNjA3RjNEOCI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOkUwMUFBQzIwRjVFQjExRUJBRjI5QzlGRDA2MDdGM0Q4IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOkUwMUFBQzIxRjVFQjExRUJBRjI5QzlGRDA2MDdGM0Q4Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+p4GKHwAAAL5JREFUeNpiDJ7rzAAFvkBcCMSmQMzDgBt8AeLTQNwHxFtAAkxQiRYg3gTEjgQMYIDKg9RthupjYAFiHyCuZiAPgPSdBLmkiIEyUAgyxIRCQ0xAhvASq3pN0h4wRgO8TKQYgAswEWMjMjtknguGehZSbMdmAE6XYFOMywCchqBrwmcAQe8Q0ozsks8UppMvIEPOUGjIaZAh/RQa0s8EzY2tZBoA0rcZFjs1QOwPxPuB+CsBjV+h6vyg+hgAAgwAvJUs2y0E6wgAAAAASUVORK5CYII=";
const QC_ICON_DONE_QC = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyRpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDYuMC1jMDA2IDc5LmRhYmFjYmIsIDIwMjEvMDQvMTQtMDA6Mzk6NDQgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCAyMi40IChXaW5kb3dzKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDozNjQwMUZEMkY1RUMxMUVCQkFEQzhDQkUyNTU1QjBBNyIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDozNjQwMUZEM0Y1RUMxMUVCQkFEQzhDQkUyNTU1QjBBNyI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjM2NDAxRkQwRjVFQzExRUJCQURDOENCRTI1NTVCMEE3IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjM2NDAxRkQxRjVFQzExRUJCQURDOENCRTI1NTVCMEE3Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+NLG4agAAAL5JREFUeNpi9Ag9wAAFvkBcCMSmQMzDgBt8AeLTQNwHxFtAAkxQiRYg3gTEjgQMYIDKg9RthupjYAFiHyCuZiAPgPSdBLmkiIEyUAgyxIRCQ0xAhvASq3r7KnswRgO8TKQYgAswEWMjMtsz7CCGehZSbMdmAE6XYFOMywCchqBrwmcAQe8Q0ozsks8UppMvIEPOUGjIaZAh/RQa0s8EzY2tZBoA0rcZFjs1QOwPxPuB+CsBjV+h6vyg+hgAAgwApRUtBUuNXo8AAAAASUVORK5CYII=";
const QC_ICON_DONE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyRpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDYuMC1jMDA2IDc5LmRhYmFjYmIsIDIwMjEvMDQvMTQtMDA6Mzk6NDQgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCAyMi40IChXaW5kb3dzKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDozNjQwMUZEMkY1RUMxMUVCQkFEQzhDQkUyNTU1QjBBNyIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDozNjQwMUZEM0Y1RUMxMUVCQkFEQzhDQkUyNTU1QjBBNyI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjM2NDAxRkQwRjVFQzExRUJCQURDOENCRTI1NTVCMEE3IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjM2NDAxRkQxRjVFQzExRUJCQURDOENCRTI1NTVCMEE3Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+NLG4agAAAL5JREFUeNpi9Ag9wAAFvkBcCMSmQMzDgBt8AeLTQNwHxFtAAkxQiRYg3gTEjgQMYIDKg9RthupjYAFiHyCuZiAPgPSdBLmkiIEyUAgyxIRCQ0xAhvASq3r7KnswRgO8TKQYgAswEWMjMtsz7CCGehZSbMdmAE6XYFOMywCchqBrwmcAQe8Q0ozsks8UppMvIEPOUGjIaZAh/RQa0s8EzY2tZBoA0rcZFjs1QOwPxPuB+CsBjV+h6vyg+hgAAgwApRUtBUuNXo8AAAAASUVORK5CYII=";

const NEW_UPLOAD_STATE = "data:image/gif;base64,R0lGODlhCwALALMLAPiLAPulAPBrAP/sQf/WAP+/JvNhAM0/AOdSAP////9+AP///wAAAAAAAAAAAAAAACH/C05FVFNDQVBFMi4wAwEAAAAh+QQFyAALACwAAAAACwALAAAELXChSaksOGN1UFFgqBjdl4Qn6SnJyY7le6ax28Kr+6qfCKojg3BIWhyOSOQiAgAh+QQFCgALACwAAAAAAwALAAAED3CBOQYY5GasO+dEsAhCBAAh+QQFCgALACwAAAAABgALAAAEHXChCQAqZeiriOfJVyhJCBLJSaictypGIC9HLQgRACH5BAUKAAsALAMAAAAGAAsAAAQfEEkEQLkjq0164gmRfKKohOg2mkq5tZ0hB/RhH4IQAQAh+QQFCgALACwGAAAABQALAAAEGXAhtFa5RKgd9FKd8oWjUIEeuqFG2y3HUUUAOw==";
const NEW_NO_STATE = "";
export const apiPaths = {
    ApiBase: process.env.VUE_APP_API_BASE,
    Login: `${process.env.VUE_APP_API_BASE}/v1/auth/signIn/`, // /api/auth/v1/signIn/
    Logout: `${process.env.VUE_APP_API_BASE}/v1/auth/signOut/`,
    RefreshToken: `${process.env.VUE_APP_API_BASE}/v1/auth/refresh-token/`,
    //RefreshToken: `${process.env.VUE_APP_API_BASE}/v1/refresh-token/`,
    buildPath: function (url) { return encodeURI(`${process.env.VUE_APP_API_BASE}${url}`); }
}

export const constants = {
    /** The key to store jwt token in a local storage.  */
    STORAGE_KEY_TOKEN: '_AUTH_TOKEN_',

    /** The key to store refresh token in a local storage */
    STORAGE_KEY_REFRESH_TOKEN: '_AUTH_REFRESH_TOKEN_',

    JWT_SUB: "sub",
    JWT_CLAIM_USERNAME: 'http://www.ecoinsight.com/jwt/claims/username',
    JWT_CLAIM_ORG: 'http://www.ecoinsight.com/jwt/claims/org',

    DATE_FORMAT: "YYYY-MM-DD",
    DATETIME_FORMAT: "YYYY-MM-DD HH:mm:ss",

    QC_ICONS: {
        NO_SUBJECT: QC_ICON_NO_SUBJECT,
        NO_DATA: QC_ICON_NO_DATA,
        NO_QC: QC_ICON_NO_QC,
        DONE_QC: QC_ICON_DONE_QC,
        DONE: QC_ICON_DONE
    },
    NEW_STATE_ICON: {
        NEW_STATE : NEW_UPLOAD_STATE,
        NEW_NO_STAGE: NEW_NO_STATE
    },

    IMAGING_TYPES: {
        DTI: 'DTI',
        T1: 'T1',
        T2: 'T2',
        FLAIR: 'FLAIR',
        PET: 'PET'
    },
    VIDEODATA_TYPES: {
        IF2001: 'IF2001',
        IF2002: 'IF2002',
        IF2003: 'IF2003',
        IF2004: 'IF2004',
        IF2005: 'IF2005',
        IF2006: 'IF2006'
    },
    PERMISSION_ROLEID: {
        ADMIN : true,
        GENRAL_MANAGER : true,
        MANAGER : true,
        ELSE : false
    },
    /**
     * 사용자 세션 만료 시간(초). 기본 60분
     */
    SESSION_TIMEOUT_SECONDS: 60 * 60,

    /**
     * 대상자 정보 업로드 파일 양식 zip file URL
     */
    SUBJECT_UPLOAD_TEMPLATE_FILE_URL: "SUBJECT_UPLOAD_TEMPLATE_FILE_URL",

    /**
     * 인체자원 반출 엑셀파일 양식 file URL
     */
    SAMPLE_TAKEOUT_UPLOAD_TEMPLATE_FILE_URL: "SAMPLE_TAKEOUT_UPLOAD_TEMPLATE_FILE_URL",

    /**
     * 데이터 사용용도: USER
     * 사용자가 등록한 데이터
     */
    ROW_USAGE_USER: "USER",
    /**
     * 데이터 사용용도: APPLICATION
     * application에서 관리하는 데이터
     */
    ROW_USAGE_APPLICATION: "APPLICATION", //a

    /**
     * 사용자 상태: 정상 (모든 활동 가능)
     */
    USER_STATUS_ACTIVE: "ACTIVE",
    USER_STATUS_ACTIVE_KR: "정상",
    USER_STATUS_ACTIVE_TITLE: "정상상태",
    /**
     * 사용자 상태: 잠김 (로그인 시도 초과 오류로 인해 잠시 잠김)
     */
    USER_STATUS_LOCKED: "LOCKED",
    USER_STATUS_LOCKED_KR: "잠김",
    USER_STATUS_LOCKED_TITLE: "로그인 오류로 인해 잠김 상태입나다.",
    /**
    * 사용자 상태: 만료 (장기 미접속일 경우)
    */
    USER_STATUS_EXPIRED: "EXPIRED",
    USER_STATUS_EXPIRED_KR: "만료",
    USER_STATUS_EXPIRED_TITLE: "장기 미접속으로 인해 만료 상태입니다.",
    /**
     * 사용자 상태: 사용안함 (모든 활동 불가능)
     */
    USER_STATUS_INACTIVE: "INACTIVE",
    USER_STATUS_INACTIVE_KR: "비활성",
    USER_STATUS_INACTIVE_TITLE: "비활성화 상태입니다.",
    /**
     * 사용자 상태: 삭제 (삭제 대상. 모든 활동 불가능)
     */
    USER_STATUS_REMOVED: "REMOVED",
    USER_STATUS_REMOVED_KR: "삭제",
    USER_STATUS_REMOVED_TITLE: "삭제 혹은 삭제대상 상태입니다."
}
export const GUEST_URL = {

     ECRF_URL: "https://rpmp.aiplatform101.com/sso"



 } 
/**
 * 기능 목록 (T_ServiceModuleFunction.FunctionId)
 */
export const FUNCTION_NAMES = {
    /**
     * 다운로드 내역
     */
    DIST_DOWNLOADHIST: "DIST_DOWNLOADHIST",
    /**
     * 영상 정보 분양
     */
    DIST_IMAGING: "DIST_IMAGING",
    /**
     * 데이터 통합 분양
     */
    DIST_MCDDATA: "DIST_MCDDATA",
    /**
     * 치매 중증도 정보 분양
     */
    DIST_MCDDEMENTIA: "DIST_MCDDEMENTIA",
    /**
     * 실험실 정보 분양
     */
    DIST_MCDLABTEST: "DIST_MCDLABTEST",
    /**
     * 신경심리 정보 분양
     */
    DIST_MCDNERO: "DIST_MCDNERO",
    /**
     * 시료 관리
     */
    DIST_SAMPLE: "DIST_SAMPLE",
    /**
     * 대상자 정보 분양
     */
    DIST_SUBJECT: "DIST_SUBJECT",

    /**
     * 영상
     */
    IMAGING: "IMAGING",


    /**
     * 사용자 활동로그
     */
    SETTING_ACT_LOG: "SETTING_ACT_LOG",
    /**
     * 참여연구원 관리
     */
    SETTING_MEMBER: "SETTING_MEMBER",
    /**
     * 참여기관 관리
     */
    SETTING_ORG: "SETTING_ORG",
    /**
     * 권한 관리
     */
    SETTING_PERMISSION: "SETTING_PERMISSION",
    /**
     * 과제 관리
     */
    SETTING_PROJECT: "SETTING_PROJECT",
    /**
     * 대상자 정보 관리
     */
    SUBJECT_MNG: "SUBJECT_MNG",

    /**
     * 문의 게시판
     */
    BOARD_QNA: "BOARD_QNA",

    /**
     * 임상 정보 통합
     */
    TRIAL_ALL : "TRIAL_ALL",

    /**
     * 디지털헬스
     */
    DIGITALHEALTH: "DIGITALHEALTH",

    /**
     *  인체유래물
     */
    HUMAN_DERIVATIVE : "HUMAN_DERIVATIVE",

    /**
     *  선별 /임상
     */
    SELECTION : "SELECTION",


    BIOMARKER : "BIOMARKER",

    PUPILLOMETRY : "PUPILLOMETRY",

    /**
     * 임상 정보 디지털 헬스 Mchat
     */
    TRIAL_DIGITALHEALTH_MCHAT : "TRIAL_DIGITALHEALTH_MCHAT",

    TRIAL_DIGITALHEALTH_EYETRACKING : "TRIAL_DIGITALHEALTH_EYETRACKING",
 
    TRIAL_DIGITALHEALTH_VIDEORESOURCE : "TRIAL_DIGITALHEALTH_VIDEORESOURCE",
    
    TRIAL_DIGITALHEALTH_PUPILLOMETRY : "TRIAL_DIGITALHEALTH_PUPILLOMETRY",
    TRIAL_BIOMARKER_FNIRS : "TRIAL_BIOMARKER_FNIRS",

    TRIAL_BIOMARKER_MICROBIOME : "TRIAL_BIOMARKER_MICROBIOME"

}

/**
 * 메뉴명 (T_Memu.MenuId)
 */
export const MENU_NAMES = {
    /**
     * 데이터 등록
     */
    DATAMNG: "DATAMNG",
    /**
     * 대상자 정보
     */
    SUBJECT: "SUBJECT",
    /**
     * 임상 정보
     */
    TRIAL: "TRIAL",
    /**
     * 데이터 분양
     */
    DIST: "DIST",
    /**
     * 데이터 분양 > 대상자 정보
     */
    DISTSUBJECT: "DISTSUBJECT",
    /**
     * 데이터 분양 > 영상 정보
     */
    DISTIMAGING: "DISTIMAGING",
    /**
     * 데이터 분양 > MCD 신경심리 검사
     */
    DISTMCDNERO: "DISTMCDNERO",
    /**
     * 데이터 분양 > MCD 치매중증도
     */
    DISTMCDDEMENTIA: "DISTMCDDEMENTIA",
    /**
     * 데이터 분양 > MCD 실험실 검사
     */
    DISTMCDLABTEST: "DISTMCDLABTEST",
    /**
     * 데이터 분양 > 데이터 통합
     */
    DISTMCDDATA: "DISTMCDDATA",
    /**
     * 데이터 분양 > 다운로드 내역
     */
    DISTMCDDOWNLOADHISTORY: "DISTMCDDOWNLOADHISTORY",
    /**
     * 데이터 분양 > 시료 관리
     */
    DISTSAMPLE: "DISTSAMPLE",
    /**
     * 데이터 분석
     */
    ANALISYS: "ANALISYS",
    /**
     * 기본설정
     */
    SETTING: "SETTING",
    /**
     * 기본설정 > 과제관리
     */
    SETTING_PROJECT: "SETTING_PROJECT",
    /**
     * 기본설정 > 참여기관 관리
     */
    SETTING_ORG: "SETTING_ORG",
    /**
     * 기본설정 > 권한관리
     */
    SETTING_PERMISSION: "SETTING_PERMISSION",
    /**
     * 기본설정 > 연구원 관리
     */
    SETTING_MEMBER: "SETTING_MEMBER",
    /**
     * 기본설정 > 활동로그 조회
     */
    SETTING_ACT_LOG: "SETTING_ACT_LOG",

    /**
     * 문의 게시판
     */
    BOARD_QNA: "BOARD_QNA"


}
  


var utc = require('dayjs/plugin/utc')
var timezone = require('dayjs/plugin/timezone') // dependent on utc plugin

export const utils = {
    /**
     * 실수값을 소숫점에 맞게 반올림하여 반환한다. (toFixed() 사용)
     * @param {Number | String} num 실수 또는 실수형 문자
     * @param {Number} decimals 소숫점 자리 수
     */
    formatFloat: function (num, decimals = 3) {
        if (!!!num || isNaN(num)) return num;

        return Number.isFinite(num) ? num.toFixed(decimals) : parseFloat(num).toFixed(decimals);
    },
    /**
     * 정수값으로 반환. (toFixed() 사용)
     * @param {string} num 정수형 문자
     */
    formatInt: function (num) {
        if (!!!num || isNaN(num)) return num;

        return Number.isFinite(num) ? num.toFixed(0) : parseFloat(num).toFixed(0);
    },
    /**
     * Date를 년-월-일 시:분:초 형식으로 포맷.
     * @param {String} date 
     * @returns 
     */
    formatDate: function (date, tz) {
        const formatted = dayjs(date).format(constants.DATETIME_FORMAT);

        return formatted;
    },

    /**
     * Date를 년-월-일 형식으로 포맷.
     * @param {String} date 
     * @returns 
     */
    formatSimpleDate: function (date) {
        return !date ? '-' : dayjs(date).format(constants.DATE_FORMAT);
    },
    /**
     * Date를 년-월-일 형식으로 포맷. (2022년 2월 21일)
     * @param {String} date 
     * @returns 
     */
    formatDateKorean: function (date) {
        const formatted = dayjs(date).format("YYYY년 MM월 DD일 HH:mm:ss");
        return formatted;
    },
    formatSimpleDateKorean: function (date) {
        const formatted = dayjs(date).format("YY.MM.DD HH:mm:ss");
        return formatted;
    },
    /**
     * 날짜에 일수를 추가해서 리턴.
     * @param {String} date 
     * @param {number} days 추가할 일수
     * @returns Date
     */
    addDays: function (date, days) {
        return dayjs(date).add(days, 'day').toDate();
    },
    /**
     * 두 날짜로 나이를 계산한다. (date2 - date1)
     * @param {Date} date1 
     * @param {Date | String} date2 
     * @returns 나이
     */
    calculateAge: function (date1, date2) {
        return !date1 || !date2 ? "-" : dayjs(date2).diff(date1, 'year');
    },
    gender: (val) => { return val == "F" ? "여" : "남"; },

    validateEmailFormat: function (email) {
        var mailformat = /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/
        return email.match(mailformat);
    },
    validateSubjectIdFormat : function(subjectId) {
        var researchNumberFormat = /^\d{10}$/;
      return  researchNumberFormat.test(subjectId);
    // return subjectId && researchNumberFormat.test(subjectId.replace(/\s/g, ''));
    },
    validatedIdFormat: function(id) {
        // var idformat = /^[a-z]+[a-z0-9]{5,19}$/g
    //    var idformat =  /^[a-z0-9]{4,12}$/gi 
    var idformat = /^(?=.*[a-z0-9])[a-z0-9]{3,10}$/gi
        return idformat.test(id)

    },
    calculateCount(count) {
        const total = + parseInt(count);
        return total;
    },
    getProjectId() {
        const params = new URLSearchParams(location.search);
        const projectid = params.get('project');
        if (projectid != undefined && Number.parseInt(projectid) > 0) {
            return Number.parseInt(projectid);
        }
        return undefined;
    },
    decoder(error){
        const decoder = new TextDecoder('utf-8');
        const jsonStr = decoder.decode(error.response.data);
        const jsonData = JSON.parse(jsonStr);

        return jsonData;
    }
    

}