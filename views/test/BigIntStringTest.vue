<template>
  <div class="bigint-test-container">
    <h1>后端字符串格式大整数ID测试</h1>
    
    <div class="test-section">
      <h2>分类数据测试</h2>
      <button @click="loadCategories">加载分类数据</button>
      <div v-if="categories.length > 0" class="data-display">
        <h3>分类列表：</h3>
        <div v-for="(category, index) in categories" :key="category.id" class="category-item">
          <p><strong>分类名称:</strong> {{ category.name }}</p>
          <p><strong>ID值:</strong> {{ category.id }}</p>
          <p><strong>ID类型:</strong> {{ typeof category.id }}</p>
          <p><strong>ID长度:</strong> {{ category.id ? category.id.length : 'N/A' }}</p>
          <p><strong>是否以4000...01格式:</strong> {{ is4000Format(category.id) }}</p>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>产地数据测试</h2>
      <button @click="loadOriginAreas">加载产地数据</button>
      <div v-if="originAreas.length > 0" class="data-display">
        <h3>产地列表：</h3>
        <div v-for="(area, index) in originAreas" :key="area.id" class="area-item">
          <p><strong>产地名称:</strong> {{ area.name }}</p>
          <p><strong>ID值:</strong> {{ area.id }}</p>
          <p><strong>ID类型:</strong> {{ typeof area.id }}</p>
          <p><strong>ID长度:</strong> {{ area.id ? area.id.length : 'N/A' }}</p>
          <p><strong>是否以4000...01格式:</strong> {{ is4000Format(area.id) }}</p>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>测试结果</h2>
      <div v-if="testResults.length > 0" class="results-display">
        <div v-for="(result, index) in testResults" :key="index" class="result-item" :class="result.success ? 'success' : 'error'">
          <p><strong>{{ result.test }}</strong>: {{ result.message }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { categoryAPI, originAreaAPI } from '../../api';
import { handleBigIntInObject } from '../../utils/bigint';

const categories = ref([]);
const originAreas = ref([]);
const testResults = ref([]);

// 检查是否是4000...01格式
const is4000Format = (id) => {
  if (!id || typeof id !== 'string') return false;
  return id.startsWith('4000') && id.endsWith('01');
};

// 加载分类数据
const loadCategories = async () => {
  try {
    testResults.value = [];
    console.log('开始加载分类数据...');
    
    const response = await categoryAPI.getTree();
    console.log('原始响应数据:', response);
    
    // 处理响应数据
    const processedData = handleBigIntInObject(response.data);
    console.log('处理后数据:', processedData);
    
    categories.value = processedData;
    
    // 测试结果
    testResults.value.push({
      test: '分类数据加载',
      success: true,
      message: `成功加载 ${processedData.length} 个分类`
    });
    
    // 检查ID格式
    let allCorrectFormat = true;
    processedData.forEach(cat => {
      if (!is4000Format(cat.id)) {
        allCorrectFormat = false;
        testResults.value.push({
          test: `分类ID格式检查: ${cat.id}`,
          success: false,
          message: `ID格式不正确，应为4000...01格式`
        });
      }
    });
    
    if (allCorrectFormat) {
      testResults.value.push({
        test: '所有分类ID格式检查',
        success: true,
        message: '所有分类ID都是正确的4000...01格式'
      });
    }
    
  } catch (error) {
    console.error('加载分类数据失败:', error);
    testResults.value.push({
      test: '分类数据加载',
      success: false,
      message: `加载失败: ${error.message}`
    });
  }
};

// 加载产地数据
const loadOriginAreas = async () => {
  try {
    testResults.value = [];
    console.log('开始加载产地数据...');
    
    const response = await originAreaAPI.getList();
    console.log('原始响应数据:', response);
    
    // 处理响应数据
    const processedData = handleBigIntInObject(response.data);
    console.log('处理后数据:', processedData);
    
    originAreas.value = processedData;
    
    // 测试结果
    testResults.value.push({
      test: '产地数据加载',
      success: true,
      message: `成功加载 ${processedData.length} 个产地`
    });
    
    // 检查ID格式
    let allCorrectFormat = true;
    processedData.forEach(area => {
      if (!is4000Format(area.id)) {
        allCorrectFormat = false;
        testResults.value.push({
          test: `产地ID格式检查: ${area.id}`,
          success: false,
          message: `ID格式不正确，应为4000...01格式`
        });
      }
    });
    
    if (allCorrectFormat) {
      testResults.value.push({
        test: '所有产地ID格式检查',
        success: true,
        message: '所有产地ID都是正确的4000...01格式'
      });
    }
    
  } catch (error) {
    console.error('加载产地数据失败:', error);
    testResults.value.push({
      test: '产地数据加载',
      success: false,
      message: `加载失败: ${error.message}`
    });
  }
};
</script>

<style scoped>
.bigint-test-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #f9f9f9;
}

button {
  padding: 10px 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 15px;
}

button:hover {
  background-color: #45a049;
}

.data-display {
  margin-top: 15px;
}

.category-item, .area-item {
  padding: 10px;
  margin-bottom: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
}

.category-item p, .area-item p {
  margin: 5px 0;
}

.results-display {
  margin-top: 15px;
}

.result-item {
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
}

.result-item.success {
  background-color: #dff0d8;
  border: 1px solid #d6e9c6;
  color: #3c763d;
}

.result-item.error {
  background-color: #f2dede;
  border: 1px solid #ebccd1;
  color: #a94442;
}
</style>