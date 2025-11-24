package com.samvitex.ui.paneles;

import com.samvitex.repositorios.RolRepositorio;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioCategoria;
import com.samvitex.servicios.ServicioCliente;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.servicios.ServicioUsuario;
import com.samvitex.ui.paneles.sub_paneles.PanelGestionAlmacenes;
import com.samvitex.ui.paneles.sub_paneles.PanelGestionCategorias;
import com.samvitex.ui.paneles.sub_paneles.PanelGestionClientes;
import com.samvitex.ui.paneles.sub_paneles.PanelGestionProveedores;
import com.samvitex.ui.paneles.sub_paneles.PanelGestionUsuarios;
import com.samvitex.ui.vistas.interfaces.GestionClientesView;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;


/**
 * Panel principal para las configuraciones del sistema.
 * Utiliza un {@link JTabbedPane} para organizar diferentes áreas de gestión
 * de catálogos, como usuarios, categorías, proveedores, clientes y almacenes.
 * Este panel obtiene las dependencias (servicios) desde el contexto de Spring
 * para inyectarlas en los sub-paneles correspondientes.
 */
@org.springframework.stereotype.Component
public class PanelConfiguracion extends JPanel {

    private JTabbedPane tabbedPane;

    /**
     * Construye el panel de configuración.
     *
     * @param context El {@link ApplicationContext} de Spring, utilizado como
     *                un localizador de servicios para obtener las dependencias
     *                necesarias para los sub-paneles.
     */
    @Autowired
    public PanelConfiguracion(ApplicationContext context) {
        // Layout principal que permite al JTabbedPane expandirse completamente.
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow]"));

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Obtención de dependencias desde el contexto de Spring ---
        // Este enfoque centraliza la obtención de beans en un solo lugar.
        ServicioUsuario servicioUsuario = context.getBean(ServicioUsuario.class);
        RolRepositorio rolRepositorio = context.getBean(RolRepositorio.class);
        ServicioCategoria servicioCategoria = context.getBean(ServicioCategoria.class);
        ServicioProveedor servicioProveedor = context.getBean(ServicioProveedor.class);
        ServicioCliente servicioCliente = context.getBean(ServicioCliente.class);
        ServicioAlmacen servicioAlmacen = context.getBean(ServicioAlmacen.class);

        // --- Creación e inserción de las pestañas de gestión ---

        // Pestaña 1: Gestión de Usuarios
        tabbedPane.addTab("Usuarios", new PanelGestionUsuarios(servicioUsuario, rolRepositorio));

        // Pestaña 2: Gestión de Categorías
        tabbedPane.addTab("Categorías", new PanelGestionCategorias(servicioCategoria));

        // Pestaña 3: Gestión de Proveedores
        tabbedPane.addTab("Proveedores", new PanelGestionProveedores(servicioProveedor));

        PanelGestionClientes panelClientes = context.getBean(PanelGestionClientes.class);
        panelClientes.setName("ClientesPanel");

        // Pestaña 4: Gestión de Clientes
        tabbedPane.addTab("Clientes", panelClientes);

        // Pestaña 5: Gestión de Almacenes
        tabbedPane.addTab("Almacenes", new PanelGestionAlmacenes(servicioAlmacen));

        // Añadir el panel de pestañas al layout principal.
        add(tabbedPane, "grow");
        this.tabbedPane = tabbedPane;
    }

    /**
     * Permite la navegación programática a una pestaña específica y la selección de un cliente.
     * @param nombrePestana El nombre de la pestaña a activar.
     * @param clienteId El ID del cliente a seleccionar.
     */
    public void seleccionarPestanaYCliente(String nombrePestana, Integer clienteId) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(nombrePestana)) {
                tabbedPane.setSelectedIndex(i);
                Component panelSeleccionado = tabbedPane.getComponentAt(i);
                if (panelSeleccionado instanceof GestionClientesView clienteView) {
                    clienteView.seleccionarYMostrarCliente(clienteId);
                }
                break;
            }
        }
    }

    /**
     * Permite la navegación programática a una pestaña específica, establece un término
     * de búsqueda y solicita la selección de un cliente.
     *
     * @param nombrePestana El nombre de la pestaña a activar.
     * @param terminoBusqueda El texto a colocar en la barra de búsqueda del panel de destino.
     * @param clienteId El ID del cliente a seleccionar.
     */
    public void seleccionarPestanaYCliente(String nombrePestana, String terminoBusqueda, Integer clienteId) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(nombrePestana)) {
                tabbedPane.setSelectedIndex(i);
                Component panelSeleccionado = tabbedPane.getComponentAt(i);
                if (panelSeleccionado instanceof PanelGestionClientes panelClientes) {
                    panelClientes.setTerminoBusqueda(terminoBusqueda);
                    panelClientes.getPresenter().seleccionarCliente(clienteId);
                }
                break;
            }
        }
    }
}