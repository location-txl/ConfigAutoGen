import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  base:'/ConfigWeaver/',
  title: "ConfigWeaver",
  description: "ConfigWeaver is a Android Gradle plugin",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    search: {
      provider: 'local'
    },
    nav: [
      { text: 'Home', link: '/' },
      { text: '指南', link: '/guide/jianjie' },
    ],

    sidebar: {
      "guide":[
        {
          text: '简介',
          items: [
            { text: '什么是ConfigWeaver', link: '/guide/introd/' },
            { text: '快速开始', link: '/guide/introd/great_start' },

          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/location-txl/ConfigWeaver' }
    ]
  }
})
