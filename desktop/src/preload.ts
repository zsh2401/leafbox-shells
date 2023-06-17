import os from 'os'
import type { ClientBridge } from '@leafbox/common'
import { contextBridge } from 'electron'

const bridgeImpl: ClientBridge = {

    getVersionCode: function (): number {
        return 1
    },

    platform: function (): NodeJS.Platform {
        return os.platform()
    },

    arch: function (): string {
        return os.arch()
    }
}
contextBridge.exposeInMainWorld(
    'leafboxNativeBridge',
    bridgeImpl
)

