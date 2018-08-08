import {API_URL} from "services/API";

import {API_COMMANDS} from "store/blynk-websocket-middleware/commands";

export function ProductsPreloadFetch() {
  return {
    type: 'API_PRODUCTS_PRELOAD_FETCH',
    ws: {
      request: {
        command: API_COMMANDS.GET_PRODUCTS
      }
    }
  };
}

export function ProductsFetch() {
  return {
    type: 'API_PRODUCTS_FETCH',
    ws: {
      request: {
        command: API_COMMANDS.GET_PRODUCTS
      }
    }
  };
}

export function ProductFetch(data) {
  if (!data.id)
    throw new Error('Missing id parameter for product fetch');

  return {
    type: 'API_PRODUCT_FETCH',
    ws: {
      request: {
        command: API_COMMANDS.GET_PRODUCT,
        query: [
          data.id,
        ],
      }
    }
  };
}

export function ProductDeleteProgress({productId}) {

  if(!productId)
    throw new Error('productId parameter is missed');

  return {
    type: 'API_PRODUCT_DELETE_PROGRESS',
    payload: {
      request: {
        method: 'delete',
        url: API_URL.ota().deleteProgress({ productId })
      }
    }
  };
}

export function ProductUpdate(data) {
  return {
    type: 'API_PRODUCT_UPDATE',
    payload: {
      request: {
        method: 'post',
        url: '/product',
        data: data
      }
    }
  };
}

export function ProductDelete(id) {

  if (isNaN(Number(id)))
    throw Error('ProductDelete is missing id parameter');

  return {
    type: 'API_PRODUCT_DELETE',
    payload: {
      request: {
        method: 'delete',
        url: `/product/${id}`
      }
    }
  };
}

export function ProductCreate(data = false) {
  return {
    type: 'API_PRODUCT_CREATE',
    payload: {
      request: {
        method: 'put',
        url: '/product',
        data: data
      }
    }
  };
}

export function ProductInfoDevicesOTAFetch({ orgId }) {
  return {
    type: 'PRODUCT_INFO_DEVICES_OTA_FETCH',
    payload: {
      request: {
        method: 'GET',
        url: API_URL.device().get({ orgId })
      }
    }
  };
}

export function ProductInfoDevicesOTAStart({ checkBoardType, title, pathToFirmware, productId, deviceIds, firmwareInfo, firmwareOriginalFileName }) {
  return {
    type: 'PRODUCT_INFO_DEVICES_OTA_START',
    payload: {
      request: {
        method: 'POST',
        url: API_URL.ota().start(),
        data: {
          title,
          checkBoardType,
          pathToFirmware,
          productId,
          deviceIds,
          firmwareInfo,
          firmwareOriginalFileName,
        }
      }
    }
  };
}

export function ProductInfoDevicesOTAStop({ productId, deviceIds }) {
  return {
    type: 'PRODUCT_INFO_DEVICES_OTA_STOP',
    payload: {
      request: {
        method: 'POST',
        url: API_URL.ota().stop(),
        data: {
          productId,
          deviceIds
        }
      }
    }
  };
}

export function ProductInfoDevicesOTAFirmwareInfoFetch({ firmwareUploadUrl }) {

  return {
    type: 'PRODUCT_INFO_DEVICES_OTA_FIRMWARE_INFO_FETCH',
    payload: {
      request: {
        method: 'GET',
        url: API_URL.ota().firmwareInto({ firmwareUploadUrl })
      }
    }
  };
}

export function ProductUpdateDevices(data = false) {
  return {
    type: 'API_PRODUCT_UPDATE_DEVICES',
    payload: {
      request: {
        method: 'POST',
        url: '/product/updateDevices',
        data: data
      }
    }
  };
}

export function CanDeleteProduct(data = {}) {
  return {
    type: 'API_PRODUCT_CAN_DELETE_PRODUCT',
    payload: {
      request: {
        method: 'GET',
        url: `/product/canDeleteProduct/${data.id}`
      }
    }
  };
}

