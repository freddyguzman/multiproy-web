/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.DetalleUsuarioTarjeta;
import cl.usach.entities.Equipo;
import cl.usach.entities.Lista;
import cl.usach.entities.Miembro;
import cl.usach.entities.Tarjeta;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.DetalleUsuarioTarjetaFacadeLocal;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.ListaFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import cl.usach.trellosessionbeans.ActividadTrelloLocal;
import cl.usach.trellosessionbeans.ListaTrelloLocal;
import cl.usach.trellosessionbeans.MiembroTrelloLocal;
import cl.usach.trellosessionbeans.TarjetaTrelloLocal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author FGT
 */
@Named(value = "escritorioManagedBean")
@ViewScoped
public class EscritorioManagedBean {
    @EJB
    private ListaFacadeLocal listaFacade;
    @EJB
    private DetalleUsuarioTarjetaFacadeLocal detalleUsuarioTarjetaFacade;
    @EJB
    private ActividadTrelloLocal actividadTrello;
    @EJB
    private MiembroTrelloLocal miembroTrello;
    @EJB
    private TarjetaTrelloLocal tarjetaTrello;
    @EJB
    private ListaTrelloLocal listaTrello;
    @EJB
    private EquipoFacadeLocal equipoFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
   
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    private DashboardModel model;
    private PieChartModel pieResumenTareas;
    
    private List<Equipo> equipos;
    private List<Tarjeta> tarjetas; 
    private Map<String,Number> grafResumenTarea;
    
    public EscritorioManagedBean() {
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }

    public List<Tarjeta> getTarjetas() {
        return tarjetas;
    }

    public void setTarjetas(List<Tarjeta> tarjetas) {
        this.tarjetas = tarjetas;
    }

    public DashboardModel getModel() {
        return model;
    }

    public Map<String, Number> getGrafResumenTarea() {
        return grafResumenTarea;
    }

    public void setGrafResumenTarea(Map<String, Number> grafResumenTarea) {
        this.grafResumenTarea = grafResumenTarea;
    }

    public PieChartModel getPieResumenTareas() {
        return pieResumenTareas;
    }
    
    @PostConstruct
    public void init(){
        actualizarEscritorio();
        crearEscritorio();        
    }
    
    public void crearEscritorio(){
        model = new DefaultDashboardModel();
        DashboardColumn column1 = new DefaultDashboardColumn();
        DashboardColumn column2 = new DefaultDashboardColumn();
        
        column1.addWidget("proyectos");        
        column1.addWidget("tareas");
        column2.addWidget("grafTareas");
        
        model.addColumn(column1);
        model.addColumn(column2);
    }
    
    public void buscarDatos(){
        Usuario usuario = usuarioFacade.buscarPorLogin(loginUsuario);
        List<Equipo> equiposBuscar = equipoFacade.buscarPorUsuario(usuario);
        
        for (Equipo equipo : equiposBuscar) {
            if("Trello".equals(equipo.getIdCuenta().getIdTipoCuenta().getNombreTipoCuenta())){                
                //Obtener o Actualizar Miembros
                miembroTrello.buscarMiembros(equipo);
                
                //Obtener o Actualizar Listas
                listaTrello.buscarListaPorTablero(equipo);              
                       
                //Obtener o Actualizar Tarjetas
                tarjetaTrello.buscarTarjetasPorLista(equipo);
                
                //Obtener Actividades
                actividadTrello.buscarActividades(equipo);
            }
            
        }
        actualizarEscritorio();        
        FacesMessage msg = new FacesMessage("Datos Actualizados","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void buscarProjectos(){
        Usuario usuario = usuarioFacade.buscarPorLogin(loginUsuario);
        equipos = equipoFacade.buscarPorUsuario(usuario);
    }
    
    public void buscarTareas(){
        List<Tarjeta> tar = new ArrayList<>();
        Map<String,Number> mapProy = new HashMap<>();
        for (Equipo equipo : equipos) {            
            Lista ultimaLista = listaFacade.buscarUltimaPorTablero(equipo.getIdTablero());
            List<Miembro> miembros = equipo.getIdCuenta().getMiembroList();
            for (Miembro miembro : miembros) {
                List<DetalleUsuarioTarjeta> detalles = 
                        detalleUsuarioTarjetaFacade.buscarPorIdMiembroYIdTableroYNoLista(miembro,equipo.getIdTablero(),ultimaLista);
                for (DetalleUsuarioTarjeta detalle : detalles) {                    
                    tar.add(detalle.getIdTarjeta());                   
                    
                    if(mapProy.containsKey(detalle.getIdTarjeta().getIdTablero().getNombreTablero())){
                        Number sum = mapProy.get(detalle.getIdTarjeta().getIdTablero().getNombreTablero()).intValue() + 1;
                        mapProy.put(detalle.getIdTarjeta().getIdTablero().getNombreTablero(), sum);
                    }else{
                        mapProy.put(detalle.getIdTarjeta().getIdTablero().getNombreTablero(),1);
                    }
                }
            }            
        }
        tarjetas = tar;
        grafResumenTarea = mapProy;
    }
    
    public void crearResumenTareaGraf(){
        pieResumenTareas = new PieChartModel();        
        pieResumenTareas.setData(grafResumenTarea);
        pieResumenTareas.setLegendPosition("e");
        pieResumenTareas.setShowDataLabels(true);
    }
    
    public void actualizarEscritorio(){        
        buscarProjectos();
        buscarTareas();
        crearResumenTareaGraf();
    }
}
