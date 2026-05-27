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
import com.marcosmoreira.mitiendadebarrio.admin.core.application.reporte.ReporteExportService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.reporte.ReporteOperativoService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.respaldo.RespaldoService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.seguridad.UsuarioLocalService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.tercero.TerceroService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.service.SystemHealthService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.venta.VentaInternaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.auditoria.SqliteAuditoriaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.ayuda.SqliteAyudaContextualRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.caja.SqliteCajaDiariaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.cartera.SqliteCarteraLocalRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.catalogo.SqliteCategoriaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.catalogo.SqliteMarcaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.catalogo.SqliteUnidadMedidaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.compra.SqliteCompraRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.config.AppProperties;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.configuracion.SqliteConfiguracionNegocioRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.contabilidad.SqliteContabilidadBasicaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.fiado.SqliteFiadoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.fiscalidad.SqliteFiscalidadPreparadaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.dashboard.SqliteDashboardRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.license.LocalLicenseService;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.inventario.SqliteInventarioFuerteRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.movimiento.SqliteMovimientoInventarioRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.opcional.SqliteOpcionalesMinimosRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.producto.SqliteProductoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.proveedor.SqliteProveedorRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.reporte.SqliteReporteOperativoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.respaldo.SqliteRespaldoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.seguridad.SqliteUsuarioLocalRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.tercero.SqliteTerceroRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.runtime.LocalRuntimeInitializer;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.runtime.RuntimePaths;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.DatabaseHealthCheck;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.LocalDatabaseMigrator;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.LocalDatabaseSeeder;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.venta.SqliteVentaInternaRepository;

/** Construye el contexto interno de la aplicación autocontenida. */
public final class AppBootstrap {
    private AppBootstrap() {}

    public static AppContext start() {
        RuntimePaths paths = RuntimePaths.defaultForUserHome();
        new LocalRuntimeInitializer(paths).ensureDirectories();

        AppProperties properties = AppProperties.loadDefault();
        SqliteConnectionFactory connectionFactory = new SqliteConnectionFactory(paths.databaseFile());
        new LocalDatabaseMigrator(connectionFactory).migrate();
        new LocalDatabaseSeeder(connectionFactory).seedInitialClientData();

        LocalLicenseService licenseService = new LocalLicenseService(paths.licenseDirectory(), connectionFactory);
        WriteAccessGuard writeAccessGuard = new WriteAccessGuard(licenseService);
        AuditoriaService auditoriaService = new AuditoriaService(new SqliteAuditoriaRepository(connectionFactory));
        UsuarioLocalService usuarioLocalService = new UsuarioLocalService(new SqliteUsuarioLocalRepository(connectionFactory), auditoriaService);
        TerceroService terceroService = new TerceroService(new SqliteTerceroRepository(connectionFactory), writeAccessGuard, auditoriaService);
        DashboardService dashboardService = new DashboardService(new SqliteDashboardRepository(connectionFactory), licenseService);
        SystemHealthService healthService = new SystemHealthService(new DatabaseHealthCheck(connectionFactory), licenseService);

        ConfiguracionNegocioService configuracionNegocioService = new ConfiguracionNegocioService(new SqliteConfiguracionNegocioRepository(connectionFactory), writeAccessGuard);
        CategoriaService categoriaService = new CategoriaService(new SqliteCategoriaRepository(connectionFactory), writeAccessGuard);
        MarcaService marcaService = new MarcaService(new SqliteMarcaRepository(connectionFactory), writeAccessGuard);
        UnidadMedidaService unidadMedidaService = new UnidadMedidaService(new SqliteUnidadMedidaRepository(connectionFactory), writeAccessGuard);
        ProveedorService proveedorService = new ProveedorService(new SqliteProveedorRepository(connectionFactory), writeAccessGuard);
        ProductoService productoService = new ProductoService(new SqliteProductoRepository(connectionFactory), writeAccessGuard);
        MovimientoInventarioService movimientoInventarioService = new MovimientoInventarioService(new SqliteMovimientoInventarioRepository(connectionFactory), writeAccessGuard);
        InventarioFuerteService inventarioFuerteService = new InventarioFuerteService(new SqliteInventarioFuerteRepository(connectionFactory), writeAccessGuard, auditoriaService);
        CompraService compraService = new CompraService(new SqliteCompraRepository(connectionFactory), writeAccessGuard, auditoriaService);
        VentaInternaService ventaInternaService = new VentaInternaService(new SqliteVentaInternaRepository(connectionFactory), writeAccessGuard, auditoriaService);
        ReporteOperativoService reporteOperativoService = new ReporteOperativoService(
                new SqliteReporteOperativoRepository(connectionFactory),
                new ReporteExportService(paths.reportsDirectory())
        );
        RespaldoService respaldoService = new RespaldoService(
                paths.databaseFile(),
                paths.backupsDirectory(),
                new SqliteRespaldoRepository(connectionFactory),
                auditoriaService
        );

        CajaDiariaService cajaDiariaService = new CajaDiariaService(new SqliteCajaDiariaRepository(connectionFactory), writeAccessGuard, auditoriaService);
        CarteraLocalService carteraLocalService = new CarteraLocalService(new SqliteCarteraLocalRepository(connectionFactory), writeAccessGuard, auditoriaService);
        FiadoService fiadoService = new FiadoService(new SqliteFiadoRepository(connectionFactory), writeAccessGuard);
        FiscalidadPreparadaService fiscalidadPreparadaService = new FiscalidadPreparadaService(new SqliteFiscalidadPreparadaRepository(connectionFactory), writeAccessGuard, auditoriaService);
        ContabilidadBasicaService contabilidadBasicaService = new ContabilidadBasicaService(new SqliteContabilidadBasicaRepository(connectionFactory), writeAccessGuard, auditoriaService);
        OpcionalesMinimosService opcionalesMinimosService = new OpcionalesMinimosService(new SqliteOpcionalesMinimosRepository(connectionFactory), writeAccessGuard, auditoriaService);
        AyudaContextualService ayudaContextualService = new AyudaContextualService(new SqliteAyudaContextualRepository(connectionFactory));
        ayudaContextualService.asegurarContenidoBase();

        return new AppContext(
                paths,
                properties,
                connectionFactory,
                healthService,
                licenseService,
                writeAccessGuard,
                usuarioLocalService,
                auditoriaService,
                terceroService,
                dashboardService,
                configuracionNegocioService,
                categoriaService,
                marcaService,
                unidadMedidaService,
                proveedorService,
                productoService,
                movimientoInventarioService,
                inventarioFuerteService,
                compraService,
                ventaInternaService,
                reporteOperativoService,
                respaldoService,
                cajaDiariaService,
                carteraLocalService,
                fiadoService,
                fiscalidadPreparadaService,
                contabilidadBasicaService,
                opcionalesMinimosService,
                ayudaContextualService
        );
    }
}
