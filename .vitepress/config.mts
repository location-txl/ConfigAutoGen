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
      { text: '指南', link: '/guide/introd/' },
      { text: '更新日志', link: ''},

    ],

    sidebar: {
      "guide":[
        {
          text: '简介',
          items: [
            { text: '什么是ConfigWeaver', link: '/guide/introd/' },
            { text: '快速开始', link: '/guide/introd/quick_start' },

          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/location-txl/ConfigWeaver' }
    ]
  }
})
