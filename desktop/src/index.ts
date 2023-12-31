import { app, BrowserWindow, globalShortcut, shell } from "electron"
import path from "path"
import { configureMenu } from "./configureMenu"
import { platform } from "os"
const createWindow = (): BrowserWindow => {

    const win = new BrowserWindow({
        title: "Leaf Box",
        width: 1200,
        height: 700,
        webPreferences: {
            preload: path.join(__dirname, "preload.js")
        }
    })

    let isQuitting = false
    app.on("before-quit", () => {
        isQuitting = true
    })

    win.on('close', function (evt) {
        if (platform() === "darwin" && !isQuitting) {
            evt.preventDefault()
            win.hide()
        }
    })
   
    win.webContents.on('will-navigate', function(event, url) {
        // const isLeafBox
        // is not leafbox website
        // is leafbox website but url contains download
        // console.log(url)
        if (!url.includes("ai.zsh2401.top") || url.includes("download")) {
            event.preventDefault();
            shell.openExternal(url);
        }
    });
    

    // Open external URL with os default browser.
    win.webContents.setWindowOpenHandler(({ url }) => {
        shell.openExternal(url);
        return { action: 'deny' };
    });

    if (process.env.ELE_DEV) {
        console.log("loading local app")
        win.loadURL("http://localhost:24011")
    } else {
        win.loadURL("https://ai.zsh2401.top")
    }
    return win
}
(async () => {

    app.setName("Leaf Box")
    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        const wins = BrowserWindow.getAllWindows()
        if (wins.length === 0) {
            createWindow()
        } else {
            wins.forEach(win => win.show())
        }
    })

    await app.whenReady()
    configureMenu(createWindow())
})()