package cn.aspes.agri.trade.controller.common;

import cn.aspes.agri.trade.common.Result;
import cn.aspes.agri.trade.converter.EntityVOConverter;
import cn.aspes.agri.trade.entity.FarmerInfo;
import cn.aspes.agri.trade.entity.FarmerProduct;
import cn.aspes.agri.trade.entity.PurchaseDemand;
import cn.aspes.agri.trade.entity.PurchaserInfo;
import cn.aspes.agri.trade.service.FarmerInfoService;
import cn.aspes.agri.trade.service.FarmerProductService;
import cn.aspes.agri.trade.service.PurchaseDemandService;
import cn.aspes.agri.trade.service.PurchaserInfoService;
import cn.aspes.agri.trade.vo.FarmerInfoVO;
import cn.aspes.agri.trade.vo.FarmerProductVO;
import cn.aspes.agri.trade.vo.PurchaseDemandVO;
import cn.aspes.agri.trade.vo.PurchaserInfoVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用搜索控制器
 */
@Tag(name = "通用搜索")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Resource
    private FarmerInfoService farmerInfoService;

    @Resource
    private PurchaserInfoService purchaserInfoService;

    @Resource
    private FarmerProductService farmerProductService;

    @Resource
    private PurchaseDemandService purchaseDemandService;

    @Resource
    private EntityVOConverter entityVOConverter;

    @Operation(summary = "根据名称关键字搜索农户")
    @GetMapping("/farmers")
    public Result<IPage<FarmerInfoVO>> searchFarmersByName(@RequestParam String keyword,
                                                        @RequestParam(defaultValue = "1") Integer current,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        Page<FarmerInfo> page = farmerInfoService.searchFarmersByName(keyword, current, size);
        IPage<FarmerInfoVO> voPage = entityVOConverter.toFarmerInfoVOPage(page);
        return Result.success(voPage);
    }

    @Operation(summary = "根据名称关键字搜索采购商")
    @GetMapping("/purchasers")
    public Result<IPage<PurchaserInfoVO>> searchPurchasersByName(@RequestParam String keyword,
                                                              @RequestParam(defaultValue = "1") Integer current,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        Page<PurchaserInfo> page = purchaserInfoService.searchPurchasersByName(keyword, current, size);
        IPage<PurchaserInfoVO> voPage = entityVOConverter.toPurchaserInfoVOPage(page);
        return Result.success(voPage);
    }

    @Operation(summary = "根据名称关键字搜索产品")
    @GetMapping("/products")
    public Result<IPage<FarmerProductVO>> searchProductsByName(@RequestParam String keyword,
                                                               @RequestParam(defaultValue = "1") int pageNum,
                                                               @RequestParam(defaultValue = "10") int pageSize) {
        IPage<FarmerProduct> page = farmerProductService.searchProductsByName(keyword, pageNum, pageSize);
        IPage<FarmerProductVO> voPage = entityVOConverter.toFarmerProductVOPage(page);
        return Result.success(voPage);
    }

    @Operation(summary = "根据产品名称关键字搜索需求")
    @GetMapping("/demands")
    public Result<IPage<PurchaseDemandVO>> searchDemandsByProductName(@RequestParam String keyword,
                                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                                      @RequestParam(defaultValue = "10") int pageSize) {
        IPage<PurchaseDemand> page = purchaseDemandService.searchDemandsByProductName(keyword, pageNum, pageSize);
        IPage<PurchaseDemandVO> voPage = entityVOConverter.toPurchaseDemandVOPage(page);
        return Result.success(voPage);
    }
}