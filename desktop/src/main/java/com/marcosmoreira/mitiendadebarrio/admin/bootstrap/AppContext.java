package com.marcosmoreira.mitiendadebarrio.admin.bootstrap;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.ayuda.AyudaContextualService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.caja.CajaDiariaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.cartera.CarteraLocalService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.CategoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.MarcaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.UnidadMedidaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.compra.CompraService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.configuracion.ConfiguracionNegocioService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.contabilidad.ContabilidadBasicaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.dashboard.DashboardService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.inventario.InventarioFuerteService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.fiado.FiadoService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.fiscalidad.FiscalidadPreparadaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.movimiento.MovimientoInventarioService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.opcional.OpcionalesMinimosService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.producto.ProductoService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.proveedor.ProveedorService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.reporte.ReporteOperativoService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.respaldo.RespaldoService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.seguridad.UsuarioLocalService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.tercero.TerceroService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.service.SystemHealthService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.venta.VentaInternaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.config.AppProperties;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.license.LocalLicenseService;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.runtime.RuntimePaths;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;

/** Contenedor mínimo de dependencias internas. */
public final class AppContext {
    private final RuntimePaths paths;
    private final AppProperties properties;
    private final SqliteConnectionFactory connectionFactory;
    private final SystemHealthService healthService;
    private final LocalLicenseService licenseService;
    private final WriteAccessGuard writeAccessGuard;
    private final UsuarioLocalService usuarioLocalService;
    private final AuditoriaService auditoriaService;
    private final TerceroService terceroService;
    private final DashboardService dashboardService;
    private final ConfiguracionNegocioService configuracionNegocioService;
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final UnidadMedidaService unidadMedidaService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;
    private final MovimientoInventarioService movimientoInventarioService;
    private final InventarioFuerteService inventarioFuerteService;
    private final CompraService compraService;
    private final VentaInternaService ventaInternaService;
    private final ReporteOperativoService reporteOperativoService;
    private final RespaldoService respaldoService;
    private final CajaDiariaService cajaDiariaService;
    private final CarteraLocalService carteraLocalService;
    private final FiadoService fiadoService;
    private final FiscalidadPreparadaService fiscalidadPreparadaService;
    private final ContabilidadBasicaService contabilidadBasicaService;
    private final OpcionalesMinimosService opcionalesMinimosService;
    private final AyudaContextualService ayudaContextualService;

    public AppContext(
            RuntimePaths paths,
            AppProperties properties,
            SqliteConnectionFactory connectionFactory,
            SystemHealthService healthService,
            LocalLicenseService licenseService,
            WriteAccessGuard writeAccessGuard,
            UsuarioLocalService usuarioLocalService,
            AuditoriaService auditoriaService,
            TerceroService terceroService,
            DashboardService dashboardService,
            ConfiguracionNegocioService configuracionNegocioService,
            CategoriaService categoriaService,
            MarcaService marcaService,
            UnidadMedidaService unidadMedidaService,
            ProveedorService proveedorService,
            ProductoService productoService,
            MovimientoInventarioService movimientoInventarioService,
            InventarioFuerteService inventarioFuerteService,
            CompraService compraService,
            VentaInternaService ventaInternaService,
            ReporteOperativoService reporteOperativoService,
            RespaldoService respaldoService,
            CajaDiariaService cajaDiariaService,
            CarteraLocalService carteraLocalService,
            FiadoService fiadoService,
            FiscalidadPreparadaService fiscalidadPreparadaService,
            ContabilidadBasicaService contabilidadBasicaService,
            OpcionalesMinimosService opcionalesMinimosService,
            AyudaContextualService ayudaContextualService
    ) {
        this.paths = paths;
        this.properties = properties;
        this.connectionFactory = connectionFactory;
        this.healthService = healthService;
        this.licenseService = licenseService;
        this.writeAccessGuard = writeAccessGuard;
        this.usuarioLocalService = usuarioLocalService;
        this.auditoriaService = auditoriaService;
        this.terceroService = terceroService;
        this.dashboardService = dashboardService;
        this.configuracionNegocioService = configuracionNegocioService;
        this.categoriaService = categoriaService;
        this.marcaService = marcaService;
        this.unidadMedidaService = unidadMedidaService;
        this.proveedorService = proveedorService;
        this.productoService = productoService;
        this.movimientoInventarioService = movimientoInventarioService;
        this.inventarioFuerteService = inventarioFuerteService;
        this.compraService = compraService;
        this.ventaInternaService = ventaInternaService;
        this.reporteOperativoService = reporteOperativoService;
        this.respaldoService = respaldoService;
        this.cajaDiariaService = cajaDiariaService;
        this.carteraLocalService = carteraLocalService;
        this.fiadoService = fiadoService;
        this.fiscalidadPreparadaService = fiscalidadPreparadaService;
        this.contabilidadBasicaService = contabilidadBasicaService;
        this.opcionalesMinimosService = opcionalesMinimosService;
        this.ayudaContextualService = ayudaContextualService;
    }

    public RuntimePaths paths() { return paths; }
    public AppProperties properties() { return properties; }
    public SqliteConnectionFactory connectionFactory() { return connectionFactory; }
    public SystemHealthService healthService() { return healthService; }
    public LocalLicenseService licenseService() { return licenseService; }
    public WriteAccessGuard writeAccessGuard() { return writeAccessGuard; }
    public UsuarioLocalService usuarioLocalService() { return usuarioLocalService; }
    public AuditoriaService auditoriaService() { return auditoriaService; }
    public TerceroService terceroService() { return terceroService; }
    public DashboardService dashboardService() { return dashboardService; }
    public ConfiguracionNegocioService configuracionNegocioService() { return configuracionNegocioService; }
    public CategoriaService categoriaService() { return categoriaService; }
    public MarcaService marcaService() { return marcaService; }
    public UnidadMedidaService unidadMedidaService() { return unidadMedidaService; }
    public ProveedorService proveedorService() { return proveedorService; }
    public ProductoService productoService() { return productoService; }
    public MovimientoInventarioService movimientoInventarioService() { return movimientoInventarioService; }
    public InventarioFuerteService inventarioFuerteService() { return inventarioFuerteService; }
    public CompraService compraService() { return compraService; }
    public VentaInternaService ventaInternaService() { return ventaInternaService; }
    public ReporteOperativoService reporteOperativoService() { return reporteOperativoService; }
    public RespaldoService respaldoService() { return respaldoService; }
    public CajaDiariaService cajaDiariaService() { return cajaDiariaService; }
    public CarteraLocalService carteraLocalService() { return carteraLocalService; }
    public FiadoService fiadoService() { return fiadoService; }
    public FiscalidadPreparadaService fiscalidadPreparadaService() { return fiscalidadPreparadaService; }
    public ContabilidadBasicaService contabilidadBasicaService() { return contabilidadBasicaService; }
    public OpcionalesMinimosService opcionalesMinimosService() { return opcionalesMinimosService; }
    public AyudaContextualService ayudaContextualService() { return ayudaContextualService; }

    public void shutdown() { connectionFactory.closeQuietly(); }
}
