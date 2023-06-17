module.exports = {
  packagerConfig: {
    asar: true,
    name: "Leaf Box",
    executableName: 'Leaf Box Desktop',
    icon: "./icons/leafbox",
  },
  rebuildConfig: {},
  makers: [
    {
      name: '@electron-forge/maker-squirrel',
      config: {},
    },
    {
      name: '@electron-forge/maker-zip',
      platforms: ['linux'],
    },
    {
      name: '@electron-forge/maker-dmg',
      platforms: ['darwin'],
      config: {
        // background: './assets/dmg-background.png',
        format: 'ULFO'
      }
    },
    // {
    //   name: '@electron-forge/maker-deb',
    //   config: {},
    // },
    // {
    //   name: '@electron-forge/maker-rpm',
    //   config: {},
    // },
  ],
  plugins: [
    {
      name: '@electron-forge/plugin-auto-unpack-natives',
      config: {},
    },
  ],
}
