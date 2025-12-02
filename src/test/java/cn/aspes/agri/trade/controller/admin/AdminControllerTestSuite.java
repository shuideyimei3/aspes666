package cn.aspes.agri.trade.controller.admin;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Admin控制器测试套件
 * 用于运行所有Admin控制器的测试
 */
@Suite
@SelectClasses({
    AdminOrderControllerTest.class,
    AdminProductControllerTest.class,
    AdminUserControllerTest.class,
    AdminCooperationControllerTest.class,
    AdminStatisticsControllerTest.class
})
public class AdminControllerTestSuite {
}