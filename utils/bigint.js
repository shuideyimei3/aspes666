/**
 * BigInt 工具函数
 * 用于处理大数值的转换和比较，确保精度无损
 */

/**
 * 安全的JSON解析器，用于处理大整数
 * @param {string} jsonString - JSON字符串
 * @returns {any} - 解析后的对象
 */
export const safeJsonParse = (jsonString) => {
  try {
    // 后端已经将大整数ID转为字符串，直接解析即可
    return JSON.parse(jsonString);
  } catch (error) {
    console.error('JSON解析失败:', error);
    throw error;
  }
};

/**
 * 将值安全地转换为 BigInt
 * @param {any} value - 要转换的值
 * @returns {BigInt|string} - 转换后的 BigInt 或原始字符串
 */
export const toBigInt = (value) => {
  if (value === null || value === undefined) {
    return value;
  }
  
  // 如果已经是 BigInt，直接返回
  if (typeof value === 'bigint') {
    return value;
  }
  
  // 如果是字符串，尝试转换为 BigInt
  if (typeof value === 'string') {
    try {
      return BigInt(value);
    } catch (e) {
      console.warn(`无法将字符串 "${value}" 转换为 BigInt:`, e);
      return value;
    }
  }
  
  // 如果是数字且超过安全整数范围，转换为 BigInt
  if (typeof value === 'number') {
    if (value > Number.MAX_SAFE_INTEGER || value < Number.MIN_SAFE_INTEGER) {
      try {
        return BigInt(value);
      } catch (e) {
        console.warn(`无法将数字 "${value}" 转换为 BigInt:`, e);
        return value.toString();
      }
    }
    return value;
  }
  
  // 其他类型，返回原始值
  return value;
};

/**
 * 处理后端返回的原始响应数据，确保大整数正确处理
 * @param {any} responseData - 后端返回的原始数据
 * @returns {any} - 处理后的数据
 */
/**
 * 处理后端返回的原始响应数据
 * @param {any} responseData - 后端返回的原始数据
 * @returns {any} - 处理后的数据
 */
export const processBackendResponse = (responseData) => {
  if (!responseData) {
    return responseData;
  }
  
  // 如果数据是字符串形式，尝试解析
  if (typeof responseData === 'string') {
    try {
      return safeJsonParse(responseData);
    } catch (error) {
      console.warn('安全JSON解析失败，使用原始数据:', error);
      return responseData;
    }
  }
  
  // 如果是对象或数组，直接返回
  // 后端已经将大整数ID转为字符串，无需额外处理
  return responseData;
};
/**
 * 将值安全地转换为字符串
 * @param {any} value - 要转换的值
 * @returns {string|any} - 转换后的字符串或原始值
 */
export const bigIntToString = (value) => {
  if (value === null || value === undefined) {
    return value;
  }
  
  // 如果是 BigInt，转换为字符串
  if (typeof value === 'bigint') {
    return value.toString();
  }
  
  // 如果是数字，转换为字符串
  if (typeof value === 'number') {
    return value.toString();
  }
  
  // 如果是字符串，直接返回（后端已经将大整数ID转为字符串）
  if (typeof value === 'string') {
    return value;
  }
  
  // 其他类型，转换为字符串
  return String(value);
};

/**
 * 递归处理对象中的大整数，将其转换为字符串
 * @param {any} obj - 要处理的对象
 * @returns {any} - 处理后的对象
 */
/**
 * 递归处理对象中的值，确保ID字段为字符串类型
 * @param {any} obj - 要处理的对象
 * @returns {any} - 处理后的对象
 */
export const handleBigIntInObject = (obj) => {
  if (obj === null || obj === undefined) {
    return obj;
  }
  
  // 处理数组
  if (Array.isArray(obj)) {
    return obj.map(item => handleBigIntInObject(item));
  }
  
  // 处理对象
  if (typeof obj === 'object') {
    const result = {};
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        // 检查是否是ID字段，确保转换为字符串
        if ((key === 'id' || key.endsWith('Id') || key.endsWith('ID') || 
             key === 'categoryId' || key === 'category_id' || 
             key === 'originAreaId' || key === 'origin_area_id' ||
             key === 'productId' || key === 'product_id' ||
             key === 'farmerId' || key === 'farmer_id' ||
             key === 'purchaserId' || key === 'purchaser_id' ||
             key === 'orderId' || key === 'order_id' ||
             key === 'contractId' || key === 'contract_id' ||
             key === 'paymentId' || key === 'payment_id' ||
             key === 'demandId' || key === 'demand_id' ||
             key === 'dockingId' || key === 'docking_id' ||
             key === 'areaId' || key === 'area_id' ||
             key === 'userId' || key === 'user_id') && 
            obj[key] !== null && 
            obj[key] !== undefined) {
          result[key] = bigIntToString(obj[key]);
        } else {
          result[key] = handleBigIntInObject(obj[key]);
        }
      }
    }
    return result;
  }
  
  // 处理基本类型
  return bigIntToString(obj);
};

/**
 * 递归处理对象中的大整数，将其转换为 BigInt
 * @param {any} obj - 要处理的对象
 * @returns {any} - 处理后的对象
 */
export const handleStringToBigIntInObject = (obj) => {
  if (obj === null || obj === undefined) {
    return obj;
  }
  
  // 处理数组
  if (Array.isArray(obj)) {
    return obj.map(item => handleStringToBigIntInObject(item));
  }
  
  // 处理对象
  if (typeof obj === 'object') {
    const result = {};
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        // 检查是否是可能的大整数ID字段
        if ((key === 'id' || key.endsWith('Id') || key.endsWith('ID')) && 
            obj[key] !== null && 
            obj[key] !== undefined) {
          result[key] = toBigInt(obj[key]);
        } else {
          result[key] = handleStringToBigIntInObject(obj[key]);
        }
      }
    }
    return result;
  }
  
  // 处理基本类型
  return toBigInt(obj);
};

/**
 * 比较两个可能的大数值是否相等
 * @param {any} a - 第一个值
 * @param {any} b - 第二个值
 * @returns {boolean} - 是否相等
 */
export const areBigIntsEqual = (a, b) => {
  if (a === b) return true;
  
  // 将两个值都转换为字符串进行比较
  const strA = bigIntToString(a);
  const strB = bigIntToString(b);
  
  return strA === strB;
};

/**
 * 检查一个值是否是大整数或可能的大整数
 * @param {any} value - 要检查的值
 * @returns {boolean} - 是否是大整数
 */
export const isBigIntValue = (value) => {
  if (value === null || value === undefined) {
    return false;
  }
  
  // 如果是 BigInt，返回 true
  if (typeof value === 'bigint') {
    return true;
  }
  
  // 如果是数字且超过安全整数范围，返回 true
  if (typeof value === 'number' && 
      (value > Number.MAX_SAFE_INTEGER || value < Number.MIN_SAFE_INTEGER)) {
    return true;
  }
  
  // 如果是字符串，尝试解析为 BigInt
  if (typeof value === 'string') {
    try {
      BigInt(value);
      return true;
    } catch (e) {
      return false;
    }
  }
  
  return false;
};

/**
 * 处理请求参数中的大整数，将其转换为字符串
 * 这个函数专门用于发送请求前的数据处理
 * @param {any} data - 要处理的数据
 * @returns {any} - 处理后的数据
 */
export const processRequestData = (data) => {
  if (data === null || data === undefined) {
    return data;
  }
  
  // 处理 FormData
  if (data instanceof FormData) {
    const formData = new FormData();
    for (let [key, value] of data.entries()) {
      if (value !== null && value !== undefined) {
        // 检查是否是需要转换为大整数字符串的字段
        if (isBigIntValue(value)) {
          formData.append(key, bigIntToString(value));
        } else {
          formData.append(key, value);
        }
      }
    }
    return formData;
  }
  
  // 处理数组
  if (Array.isArray(data)) {
    return data.map(item => processRequestData(item));
  }
  
  // 处理对象
  if (typeof data === 'object') {
    const result = {};
    for (const key in data) {
      if (data.hasOwnProperty(key)) {
        // 检查是否是可能的大整数ID字段
        if (isBigIntValue(data[key])) {
          result[key] = bigIntToString(data[key]);
        } else {
          result[key] = processRequestData(data[key]);
        }
      }
    }
    return result;
  }
  
  // 处理基本类型
  return bigIntToString(data);
};

/**
 * 处理响应数据中的值，确保ID字段为字符串类型
 * 这个函数专门用于接收响应后的数据处理
 * @param {any} data - 要处理的数据
 * @returns {any} - 处理后的数据
 */
export const processResponseData = (data) => {
  if (data === null || data === undefined) {
    return data;
  }
  
  // 处理数组
  if (Array.isArray(data)) {
    return data.map(item => processResponseData(item));
  }
  
  // 处理对象
  if (typeof data === 'object') {
    const result = {};
    for (const key in data) {
      if (data.hasOwnProperty(key)) {
        // 检查是否是ID字段，确保转换为字符串
        if ((key === 'id' || key.endsWith('Id') || key.endsWith('ID') || 
             key === 'categoryId' || key === 'category_id' || 
             key === 'originAreaId' || key === 'origin_area_id' ||
             key === 'productId' || key === 'product_id' ||
             key === 'farmerId' || key === 'farmer_id' ||
             key === 'purchaserId' || key === 'purchaser_id' ||
             key === 'orderId' || key === 'order_id' ||
             key === 'contractId' || key === 'contract_id' ||
             key === 'paymentId' || key === 'payment_id' ||
             key === 'demandId' || key === 'demand_id' ||
             key === 'dockingId' || key === 'docking_id' ||
             key === 'areaId' || key === 'area_id' ||
             key === 'userId' || key === 'user_id') && 
            data[key] !== null && 
            data[key] !== undefined) {
          // 使用 bigIntToString 确保值转换为字符串
          result[key] = bigIntToString(data[key]);
        } else {
          result[key] = processResponseData(data[key]);
        }
      }
    }
    return result;
  }
  
  // 处理基本类型
  return bigIntToString(data);
};