# **Camera**X

```
ListenableFuture<ProcessCameraProvider>
```

1. **获取一个用于管理相机生命周期的 ProcessCameraProvider 实例**，并且以异步的方式返回这个实例
2. 配置ImageAnalysis为非阻塞模式STRATEGY_KEEP_ONLY_LATEST
3. 配置setAnalyzer帧分析器
