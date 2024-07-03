import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  base:'/ConfigWeaver/',
  lastUpdated: true,
  markdown: {
    lineNumbers: true,
  },
  title: "ConfigWeaver",
  description: "ConfigWeaver is a Android Gradle plugin",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    search: {
      provider: 'local'
    },
    lastUpdated: {
      text: 'Updated at',
      formatOptions: {
        dateStyle: 'short',
      }
    },
    outline:{
      level:[2,3],
      label:"页面导航"
    },
    docFooter: {
      prev: '上一页',
      next: '下一页'
    },
    nav: [
      { text: 'Home', link: '/' },
      { text: '指南', link: '/guide/intro/' },
      { text: '更新日志', link: ''},
    ],

    sidebar: {
      "guide":[
        {
          text: '简介',
          items: [
            { text: 'ConfigWeaver是什么', link: '/guide/intro/' },
            { text: '快速开始', link: '/guide/intro/quick_start' },
          ]
        },
        {
          text: 'json配置',
          items: [
            { text: '合并规则', link: '/guide/json/merge_rule' },
            { text: '快速开始', link: '/guide/intro/quick_start' },
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/location-txl/ConfigWeaver' }
    ]
  }
})
