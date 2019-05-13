/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

// See https://docusaurus.io/docs/site-config for all the possible
// site configuration options.

// List of projects/orgs using your project for the users page.
const users = [
  /*
  This is how a user description should look. The description field is optional.
  You can use markdown in your company description.
  {
    caption: "The name of your company",
    image: "/img/your-company-logo.png",
    description: "Describe how your company uses bitcoin-s",
    pinned: true
  },
  */
  {
    caption: "Suredbits",
    image: "/img/suredbits-logo.png",
    infoLink: "https://suredbits.com",
    description: "Suredbits uses Bitcoin-S to power their Lightning APIs.",
    pinned: true
  },
  {
    caption: "Gemini",
    image: "/img/gemini-logo.png",
    infoLink: "https://gemini.com",
    description: [
      "Gemini uses Bitcoin-S to foo bar baz.",
      "Read more at [their blog](https://medium.com/gemini/gemini-upgrades-wallet-with-full-support-of-segwit-5bb8e4bc851b)"
    ].join(" "),
    pinned: true
  }
];

const baseUrl = "/";
const siteConfig = {
  title: "bitcoin-s", // Title for your website.
  tagline: "Bitcoin implementation in Scala",
  url: "https://bitcoin-s.org", // Your website URL
  baseUrl, // Base URL for your project */
  // For github.io type URLs, you would set the url and baseUrl like:
  //   url: 'https://facebook.github.io',
  //   baseUrl: '/test-site/',

  // URL for editing docs, has to be present for the
  // "Edit this Doc" button to appear
  editUrl: "https://github.com/bitcoin-s/bitcoin-s-core/docs",

  // Used for publishing and more
  projectName: "bitcoin-s",
  organizationName: "bitcoin-s",
  // For top-level user or org sites, the organization is still the same.
  // e.g., for the https://JoelMarcey.github.io site, it would be set like...
  //   organizationName: 'JoelMarcey'

  // For no header links in the top nav bar -> headerLinks: [],
  headerLinks: [
    { doc: "core/core-intro", label: "Docs" },
    { href: baseUrl + "api", label: "API" },
    { page: "help", label: "Help" },
    { blog: true, label: "Blog" }
  ],

  // If you have users set above, you add it here:
  users,

  /* path to images for header/footer */
  headerIcon: "img/favicon.ico",
  footerIcon: "img/favicon.ico",
  favicon: "img/favicon.ico",

  /* Colors for website */
  colors: {
    primaryColor: "#1f7a8c", // teal
    secondaryColor: "#bfdbf7" // light-ish blue
  },

  /* Custom fonts for website */
  /*
  fonts: {
    myFont: [
      "Times New Roman",
      "Serif"
    ],
    myOtherFont: [
      "-apple-system",
      "system-ui"
    ]
  },
  */

  // This copyright info is used in /core/Footer.js and blog RSS/Atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()} Suredbits & the bitcoin-s developers`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks.
    theme: "default"
  },

  // Add custom scripts here that would be placed in <script> tags.
  scripts: ["https://buttons.github.io/buttons.js"],

  // On page navigation for the current documentation page.
  onPageNav: "separate",
  // No .html extensions for paths.
  cleanUrl: true,

  // Open Graph and Twitter card images.
  ogImage: "img/undraw_online.svg",
  twitterImage: "img/undraw_tweetstorm.svg",

  // Show documentation's last contributor's name.
  enableUpdateBy: true,

  // Show documentation's last update time.
  enableUpdateTime: true,

  // don't use Docusarus CSS for Scaladocs,
  // and don't let Scaladoc CSS influence
  // Docusaurus
  separateCss: ["api"],

  // mdoc writes docs to this directory
  customDocsPath: "bitcoin-s-docs/target/mdoc",

  ////////////////////
  // custom keys begin
  repoUrl: "https://github.com/bitcoin-s/bitcoin-s-core",
  suredbitsSlack:
    "https://join.slack.com/t/suredbits/shared_invite/enQtNDEyMjY3MTg1MTg3LTYyYjkwOGUzMDQ4NDAwZjE1M2I3MmQyNWNlZjNlYjg4OGRjYTRjNWUwNjRjNjg4Y2NjZjAxYjU1N2JjMTU1YWM",
  gitterUrl: "https://gitter.im/bitcoin-s-core/"
  // custom keys end
  //////////////////
};

module.exports = siteConfig;