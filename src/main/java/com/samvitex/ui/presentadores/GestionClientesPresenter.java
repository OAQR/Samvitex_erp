package com.samvitex.ui.presentadores;

import com.samvitex.modelos.entidades.Cliente;
import com.samvitex.servicios.ServicioCliente;
import com.samvitex.ui.vistas.interfaces.GestionClientesView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Presenter para la vista de Gestión de Clientes.
 * Contiene la lógica de la UI para las operaciones CRUD de clientes.
 */
@Component
public class GestionClientesPresenter {

    private GestionClientesView view;
    private final ServicioCliente servicioCliente;

    private Integer idClienteASeleccionar = null;
    private String terminoBusqueda = "";

    @Autowired
    public GestionClientesPresenter(ServicioCliente servicioCliente) {
        this.servicioCliente = servicioCliente;
    }

    /**
     * Establece la vista que este presentador controlará.
     * @param view La instancia de la GestionClientesView.
     */
    public void setView(GestionClientesView view) {
        this.view = view;
    }

    /**
     * Carga la lista completa de clientes de forma asíncrona.
     */
    public void cargarClientes() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    if (terminoBusqueda != null && !terminoBusqueda.isBlank()) {
                        return servicioCliente.buscarClientes(terminoBusqueda);
                    } else {
                        return servicioCliente.obtenerTodos();
                    }
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarClientes(get());
                    if (idClienteASeleccionar != null) {
                        view.seleccionarYMostrarCliente(idClienteASeleccionar);
                        idClienteASeleccionar = null;
                    }
                } catch (Exception e) {
                    view.mostrarError("Error al cargar los clientes: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Inicia una nueva búsqueda de clientes basada en el texto proporcionado por la vista.
     *
     * @param termino El nuevo término de búsqueda.
     */
    public void buscarClientes(String termino) {
        this.terminoBusqueda = termino;
        cargarClientes();
    }

    /**
     * Inicia el proceso para la creación de un nuevo cliente.
     */
    public void onNuevoClienteClicked() {
        view.mostrarDialogoCliente(null);
    }

    /**
     * Inicia el proceso para la edición de un cliente existente.
     *
     * @param cliente El cliente seleccionado en la tabla de la vista.
     */
    public void onEditarClienteClicked(Cliente cliente) {
        if (cliente == null) {
            view.mostrarError("Debe seleccionar un cliente para poder editarlo.");
            return;
        }
        view.mostrarDialogoCliente(cliente);
    }

    /**
     * Inicia el proceso para la desactivación de un cliente.
     *
     * @param cliente El cliente seleccionado para desactivar.
     */
    public void onDesactivarClienteClicked(Cliente cliente) {
        if (cliente == null) {
            view.mostrarError("Debe seleccionar un cliente para poder desactivarlo.");
            return;
        }
        String mensaje = String.format("¿Confirmas que deseas desactivar al cliente '%s'?", cliente.getNombreCompleto());
        if (view.confirmarAccion(mensaje, "Confirmar Desactivación")) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioCliente.desactivar(cliente.getId());
                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("El cliente ha sido desactivado.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        view.mostrarError("No se pudo desactivar el cliente: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }

    /**
     * Inicia el flujo para seleccionar un cliente específico en la vista.
     * Establece un flag con el ID del cliente y luego dispara una recarga completa
     * de los datos de la vista. La selección real ocurrirá una vez que la carga finalice.
     *
     * @param clienteId El ID del cliente a seleccionar.
     */
    public void seleccionarCliente(Integer clienteId) {
        this.idClienteASeleccionar = clienteId;
        cargarClientes();
    }

    private void handleError(Exception e, String context) {
        String message = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        view.mostrarError(String.format("%s: %s", context, message));
    }
}