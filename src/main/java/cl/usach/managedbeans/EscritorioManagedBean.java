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
import cl.usach.kanbanizesessionbeans.ActividadKanbanizeLocal;
import cl.usach.kanbanizesessionbeans.ListaKanbanizeLocal;
import cl.usach.kanbanizesessionbeans.MiembroKanbanizeLocal;
import cl.usach.kanbanizesessionbeans.TarjetaKanbanizeLocal;
import cl.usach.sessionbeans.DetalleUsuarioTarjetaFacadeLocal;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.ListaFacadeLocal;
import cl.usach.sessionbeans.MiembroFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import cl.usach.trellosessionbeans.ActividadTrelloLocal;
import cl.usach.trellosessionbeans.ListaTrelloLocal;
import cl.usach.trellosessionbeans.MiembroTrelloLocal;
import cl.usach.trellosessionbeans.TarjetaTrelloLocal;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
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
    private MiembroFacadeLocal miembroFacade;
    @EJB
    private ActividadKanbanizeLocal actividadKanbanize;
    @EJB
    private TarjetaKanbanizeLocal tarjetaKanbanize;
    @EJB
    private ListaKanbanizeLocal listaKanbanize;
    @EJB
    private MiembroKanbanizeLocal miembroKanbanize;
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
    private PieChartModel pieResumenHorasTareas;
    
    private List<Equipo> equipos;
    private List<Tarjeta> tarjetas; 
    private Map<String,Number> grafResumenTarea;
    private Map<String,Number> grafResumenHorasTarea;
    
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

    public Map<String, Number> getGrafResumenHorasTarea() {
        return grafResumenHorasTarea;
    }

    public PieChartModel getPieResumenHorasTareas() {
        return pieResumenHorasTareas;
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
        column2.addWidget("grafHorasTareas");
        
        model.addColumn(column1);
        model.addColumn(column2);
    }
    
    public void buscarDatos() throws IOException{        
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
            if("Kanbanize".equals(equipo.getIdCuenta().getIdTipoCuenta().getNombreTipoCuenta())){
                //Obtener o actualizar Miembros
                miembroKanbanize.buscarMiembos(equipo);
                //Obtener o Actualizar Listas
                listaKanbanize.buscarListas(equipo);
                //Obtener o Actualizar Tarjetas
                tarjetaKanbanize.buscarTarjetas(equipo);
                //Obtener Actividades
                actividadKanbanize.buscarActividades(equipo);
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
        Map<String,Number> mapHoras = new HashMap<>();       
        for (Equipo equipo : equipos) {            
            Lista ultimaLista = listaFacade.buscarUltimaPorTablero(equipo.getIdTablero());
            List<Miembro> miembros = miembroFacade.buscarPoIdTableroYIdCuenta(equipo.getIdTablero(),equipo.getIdCuenta());
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
                    
                    if(detalle.getIdTarjeta().getIdEstadoTarjeta().getNombreEstadoTarjeta().equals("En proceso")){
                        if(mapHoras.containsKey(detalle.getIdTarjeta().getIdTablero().getNombreTablero())){
                            if(detalle.getIdTarjeta().getIdEstadoTarjeta().getNombreEstadoTarjeta().equals("Terminado")){
                                int tiempo = obtenerSegundosDiff(detalle.getIdTarjeta().getFechaInicioTarjeta(),
                                        detalle.getIdTarjeta().getFechaFinalTarjeta());
                                Number sum = mapHoras.get(detalle.getIdTarjeta().getIdTablero().getNombreTablero()).intValue() + tiempo;
                                mapHoras.put(detalle.getIdTarjeta().getIdTablero().getNombreTablero(), sum);
                            }else{
                                int tiempo = obtenerSegundosDiff(detalle.getIdTarjeta().getFechaInicioTarjeta()
                                        ,new Date());
                                Number sum = mapHoras.get(detalle.getIdTarjeta().getIdTablero().getNombreTablero()).intValue() + tiempo;
                                mapHoras.put(detalle.getIdTarjeta().getIdTablero().getNombreTablero(), sum);
                            }
                        }else{
                            if(detalle.getIdTarjeta().getIdEstadoTarjeta().getNombreEstadoTarjeta().equals("Terminado")){
                                int tiempo = obtenerSegundosDiff(detalle.getIdTarjeta().getFechaInicioTarjeta(),
                                        detalle.getIdTarjeta().getFechaFinalTarjeta());
                                mapHoras.put(detalle.getIdTarjeta().getIdTablero().getNombreTablero(), tiempo);
                            }else{
                                int tiempo = obtenerSegundosDiff(detalle.getIdTarjeta().getFechaInicioTarjeta()
                                        ,new Date());
                                mapHoras.put(detalle.getIdTarjeta().getIdTablero().getNombreTablero(), tiempo);
                            }
                        }
                    }                    
                }
            }            
        }
        
        tarjetas = tar;
        grafResumenTarea = mapProy;
        grafResumenHorasTarea = mapHoras;
    }
    
    public void crearResumenTareaGraf(){
        pieResumenTareas = new PieChartModel();        
        pieResumenTareas.setData(grafResumenTarea);
        pieResumenTareas.setLegendPosition("e");
        pieResumenTareas.setShowDataLabels(true);
    }
    
    public void crearResumenHorasTareaGraf(){
        pieResumenHorasTareas = new PieChartModel(grafResumenHorasTarea);
        pieResumenHorasTareas.setLegendPosition("e");
        pieResumenHorasTareas.setShowDataLabels(true);
    }
    
    public void actualizarEscritorio(){
        buscarProjectos();
        buscarTareas();
        crearResumenTareaGraf();
        crearResumenHorasTareaGraf();
    }
    
    public int obtenerSegundosDiff(Date inicio, Date fin){
        DateTime dtI = new DateTime(inicio);
        DateTime dtF = new DateTime(fin);        
        int tiempo = Seconds.secondsBetween(dtI, dtF).getSeconds(); 
        return tiempo;
    }
    
    public String obtenerTiempo(Date inicio){
        if(inicio == null) return "-";
        DateTime dtI = new DateTime(inicio);
        DateTime dtF = new DateTime(new Date());
        String tiempo = "";
        String aux;
        if(Days.daysBetween(dtI, dtF).getDays() > 0) tiempo += Days.daysBetween(dtI, dtF).getDays() + " d, ";
        aux = Hours.hoursBetween(dtI, dtF).getHours() % 24 + ":";
        if(aux.length() == 2) aux = "0"+aux;
        tiempo += aux;
        aux = Minutes.minutesBetween(dtI, dtF).getMinutes() % 60 + "";
        if(aux.length() == 1) aux = "0"+aux;
        tiempo += aux + " hrs";
        return tiempo;
    }
    
    public String obtenerTiempoProyecto(String nombre){
        String tiempo = "-";
        if(grafResumenHorasTarea.containsKey(nombre)){
            Number numero = grafResumenHorasTarea.get(nombre);
            long secondsTime = numero.longValue();
            int segundos = (int) secondsTime % 60;
            secondsTime -= segundos;
            long minutesTime = secondsTime / 60;
            long minutos = minutesTime % 60;
            minutesTime -= minutos;
            long hoursTime = minutesTime / 60;
            long horas = hoursTime % 24;
            hoursTime -= horas;
            long dias = hoursTime / 24;
            tiempo = "" + dias + " dia(s), " + horas + " hora(s), " + minutos +  " minuto(s), " + segundos + " segundo(s)";
        }        
        return tiempo;
    }
    
    public String formatFecha(Date fecha){
        if(fecha == null) return "-";        
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String datef = df.format(fecha);
        return datef;
    }   
}
