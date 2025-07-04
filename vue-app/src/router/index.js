import { createRouter, createWebHistory } from 'vue-router'
import { constants } from '../services/constants'

const routes = [  
  {
    path: '/',
    name: 'Main',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
      children:[
        {
          path: '/dashboard',
          name: 'DashBoard',
          component: () => import('../views/analysis/DashBoard.vue')
        },
        {
          path: '/subject',
          name: 'Subject',
          component: () => import('../views/Subject.vue')
        },
        {
          path: '/trial',
          name: 'Trial',
          component: () => import('../views/Trial.vue')
        },
        {
          path: '/trial/mri',
          name: 'TrialImaging',
          component: () => import('../views/trials/Imaging.vue'),
          // children: [
          //   {
          //     path: '/trial/imaging/:id',
          //     name: 'TrialImagingDetail',
          //     component: () => import('../views/trials/ImagingDetail.vue')
          //   },
          // ]
        },
        {
          path: '/trial/mchat',
          name: 'TrialDigitalHealthMchat',
          component: () => import('../views/trials/DigitalHealthMchat.vue')          
        },
        {
          path: '/trial/eyetracking',
          name: 'TrialDigitalHealthEyetracking',
          component: () => import('../views/trials/DigitalHealthEyetracking.vue')          
        },
      
        {
          path: '/trial/videoresource',
          name: 'TrialDigitalHealthVideoResource',
          component: () => import('../views/trials/DigitalHealthVideoResource.vue')          
        },

        {
          path: '/trial/selection',
          name: 'TrialSelection',
          component: () => import('../views/trials/Selection.vue')          
        },
        {
          path: '/trial/pupillometry',
          name: 'TrialDigitalHealthPupillometry',
          component: () => import('../views/trials/DigitalHealthPupillometry.vue')          
        },
        {
          path: '/trial/fnirsresource',
          name: 'DigitalHealthFnirsResource',
          component: () => import('../views/trials/DigitalHealthFnirsResource.vue')          
        },
        {
          path: '/trial/microbiome',
          name: 'Microbiome',
          component: () => import('../views/trials/Microbiome.vue')          
        },
        {
          path: '/project',
          name: 'Project',
          component: () => import('../views/Project.vue')
        },
        {
          path: '/organization',
          name: 'Organization',
          component: () => import('../views/Organization.vue')
        },
        {
          path: '/permission',
          name: 'Permission',
          component: () => import('../views/Permission.vue')
        },
        {
          path: '/member',
          name: 'Member',
          component: () => import('../views/Member.vue')
        },       
        {
          path: '/system/member-activity',
          name: 'MemberActivityLog',
          component: () => import('../views/system-management/ActivityLog.vue')
        },    
        {
          path: '/board',
          component: () => import('../views/board/BoardHome.vue'),
          children: [
            {
              path: '',
              name: 'BoardList',
              component: () => import('../views/board/BoardList.vue'),
            },
            {
              path: ':boardItemId',
              name: 'BoardDetail',
              props: true,
              component: () => import('../views/board/BoardDetail.vue'),
            },
            {
              path: ':boardid/edit',
              name: 'BoardEdit',
              props: true,
              component: () => import('../views/board/BoardEdit.vue'),
            }
          ]
        },
        {
          path: '/errors/no-permission',
          name: 'NoPermissionError',
          component: () => import('../views/errors/NoPermission.vue')
        },
      ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
  },
  {
    path: '/logout',
    name: 'Logout',
    component: () => import('../views/Logout.vue')
  },
  {
    path: '/mriviewer',
    name: 'MriViewer',
    component: () => import('../views/MriViewer.vue'),
    props: (route) => ( {
      seriesId: route.query.seriesId,
      targetUrl: route.query.targetUrl
    })
  },
  // Router for ONMI board
  {
    path: '/mobile/omni/board',
    component: () => import('../views/mobile/omni/BoardHome.vue'),
    children: [
      {
        path: '',
        name: 'MobileOmniBoardList',
        component: () => import('../views/mobile/omni/BoardList.vue'),
      },
      {
        path: ':boardItemId',
        name: 'MobileOmniBoardDetail',
        component: () => import('../views/mobile/omni/BoardDetail.vue'),
      },
      {
        path: 'upload',
        name: 'MobileOmniBoardUpload',
        component: () => import('../views/mobile/omni/BoardUpload.vue'),
      },
      {
        path: ':boardid/edit',
        name: 'MobileOmniBoardEdit',
        component: () => import('../views/mobile/omni/BoardEdit.vue'),
      }
    ]
  }, 
  {
    path: '/policypage',
    name: 'PolicyPage',
    component: () => import('../views/mobile/policy/PolicyPage.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

router.beforeEach((to, from, next)=>{
  const guestPages = ['/login','/mobile/omni/board','/policypage'];
  const authRequired = !guestPages.includes(to.path);
  const loggedIn = sessionStorage.getItem(constants.STORAGE_KEY_TOKEN);


  if (authRequired && !loggedIn) {
    next('/login');
  }
  else {
    next();
  }
});

export default router
