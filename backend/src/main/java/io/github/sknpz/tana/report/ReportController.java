package io.github.sknpz.tana.report;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Relatórios")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Indicadores do dashboard e valor do estoque por categoria")
    @GetMapping("/summary")
    public InventorySummaryResponse summary() {
        return reportService.summary();
    }

    @Operation(summary = "Produtos com maior valor em estoque (quantidade x preço unitário)")
    @GetMapping("/top-products")
    public List<TopProductResponse> topProducts(
            @Parameter(description = "Quantidade de produtos (1 a 50)") @RequestParam(defaultValue = "5") int limit) {
        return reportService.topProducts(limit);
    }
}
