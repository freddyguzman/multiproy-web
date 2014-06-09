/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Asignatura;
import cl.usach.entities.DetalleUsuarioTarjeta;
import cl.usach.entities.Equipo;
import cl.usach.entities.Miembro;
import cl.usach.entities.SprintAsignatura;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.AsignaturaFacadeLocal;
import cl.usach.sessionbeans.DetalleUsuarioTarjetaFacadeLocal;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.MiembroFacadeLocal;
import cl.usach.sessionbeans.SprintAsignaturaFacadeLocal;
import cl.usach.sessionbeans.SprintGruposFacadeLocal;
import cl.usach.sessionbeans.TarjetaFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import util.Semana;

/**
 *
 * @author FGT
 */
@Named(value = "trabajoSemanalManagedBean")
@ViewScoped
public class TrabajoSemanalManagedBean {
    @EJB
    private SprintAsignaturaFacadeLocal sprintAsignaturaFacade;
    @EJB
    private SprintGruposFacadeLocal sprintGruposFacade;
    @EJB
    private AsignaturaFacadeLocal asignaturaFacade;
    @EJB
    private DetalleUsuarioTarjetaFacadeLocal detalleUsuarioTarjetaFacade;
    @EJB
    private MiembroFacadeLocal miembroFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private EquipoFacadeLocal equipoFacade;
    
    private final SesionManagedBean sesionManagedBean = new SesionManagedBean();
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    private Usuario usuario;
    
    private List<Equipo> equipos;
    private Equipo equipoSeleccionado;
    private int idEquipoSeleccionado;
    private List<Asignatura> asignaturas;
    private int idAsignaturaSeleccionada;
    private int tipoGrafico;
    
    private BarChartModel barModel;
    private String title;

    
    public TrabajoSemanalManagedBean() {
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public Equipo getEquipoSeleccionado() {
        return equipoSeleccionado;
    }

    public void setEquipoSeleccionado(Equipo equipoSeleccionado) {
        this.equipoSeleccionado = equipoSeleccionado;
    }

    public int getIdEquipoSeleccionado() {
        return idEquipoSeleccionado;
    }

    public void setIdEquipoSeleccionado(int idEquipoSeleccionado) {
        this.idEquipoSeleccionado = idEquipoSeleccionado;
    }

    public List<Asignatura> getAsignaturas() {
        return asignaturas;
    }

    public void setAsignaturas(List<Asignatura> asignaturas) {
        this.asignaturas = asignaturas;
    }

    public int getIdAsignaturaSeleccionada() {
        return idAsignaturaSeleccionada;
    }

    public void setIdAsignaturaSeleccionada(int idAsignaturaSeleccionada) {
        this.idAsignaturaSeleccionada = idAsignaturaSeleccionada;
    }

    public BarChartModel getBarChartModel() {
        return barModel;
    }

    public String getTitle() {
        return title;
    }

    public int getTipoGrafico() {
        return tipoGrafico;
    }

    public void setTipoGrafico(int tipoGrafico) {
        this.tipoGrafico = tipoGrafico;
    }
    
    @PostConstruct
    public void init(){
        inicializarCharCero();
        
        if(sesionManagedBean.checkUserAlumno()){            
            usuario = usuarioFacade.buscarPorLogin(loginUsuario);
            List<Asignatura> aux = new ArrayList<>();
            for (Equipo equipo : equipoFacade.buscarPorUsuarioGBYAsignatura(usuario)) {
                aux.add(equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getIdAsignatura());
            }            
            asignaturas = aux;
        }else{
            usuario = usuarioFacade.buscarPorLogin(loginUsuario);
            asignaturas = asignaturaFacade.buscarPorIdUsuario(usuario);            
        }
    }
    
    public void inicializarCharCero(){
        barModel = new BarChartModel();
        ChartSeries serie1 = new ChartSeries();
        serie1.set(0, 0);
        barModel.addSeries(serie1);
        barModel.setTitle("Buscar Asignatura");
        barModel.setLegendPosition("e");
        barModel.setShowPointLabels(true);
        barModel.getAxes().put(AxisType.X, new CategoryAxis("Semanas"));
        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("Horas");
        yAxis.setMin(0);
    }
    
    public void construirGrafico(){
        barModel = buscarGrafico();
        barModel.setLegendPosition("e");
        barModel.setShowPointLabels(true);
        barModel.getAxes().put(AxisType.X, new CategoryAxis("Semanas"));
        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("Horas");
        yAxis.setMin(0);
    }
    
    public BarChartModel buscarGrafico(){
        BarChartModel model = new BarChartModel();
        Asignatura asig = asignaturaFacade.buscarPorId(idAsignaturaSeleccionada);
        if(sesionManagedBean.checkUserAlumno()){            
            if(sprintGruposFacade.existePorUsuarioSMasterYAsignatura(usuario, asig)){
                List<Usuario> usuarios = equipoFacade.buscarUsuariosPorAsignatura(asig);
                for (Usuario usr : usuarios) {
                    List<Equipo> eqs = equipoFacade.buscarPorUsuarioyAsignatura(usr, asig);
                    ChartSeries serie = buscarGraficoPorAsignaturaYEquipo(asig, eqs, usr.getNombreUsuario());
                    model.addSeries(serie);
                    model.setTitle(asig.getNombreAsignatura());
                }
            }else{
                List<Equipo> eqs = equipoFacade.buscarPorUsuarioyAsignatura(usuario, asig);
                if(!eqs.isEmpty()){
                    ChartSeries serie = buscarGraficoPorAsignaturaYEquipo(asig,eqs, usuario.getNombreUsuario());
                    model.addSeries(serie);
                    model.setTitle(asig.getNombreAsignatura()); 
                }else{
                    ChartSeries serie1 = new ChartSeries();
                    serie1.set(0, 0);
                    model.addSeries(serie1);
                }                
            }
        }else if(sesionManagedBean.checkUserAdminAndProfesor()){
            List<Usuario> usuarios = equipoFacade.buscarUsuariosPorAsignatura(asig);
            if(tipoGrafico == 0){
                ChartSeries serie = buscarDatosProfesor(asig, usuarios);
                model.addSeries(serie);
                model.setTitle(asig.getNombreAsignatura());
            }else{
                for (Usuario usr : usuarios) {
                    List<Equipo> eqs = equipoFacade.buscarPorUsuarioyAsignatura(usr, asig);
                    ChartSeries serie = buscarGraficoPorAsignaturaYEquipo(asig, eqs, usr.getNombreUsuario());
                    model.addSeries(serie);
                    model.setTitle(asig.getNombreAsignatura());
                } 
            }            
        }
        
        if(model.getSeries().isEmpty()){            
            ChartSeries serie1 = new ChartSeries();
            serie1.set(0, 0);
            model.addSeries(serie1);
        }
        
        return model;
    }
    
    public ChartSeries buscarGraficoPorAsignaturaYEquipo(Asignatura asig, List<Equipo> eqs, String nombreUsuario){       
        String fechaAux;
        Map<String,Integer> aux = buscarSemanasSprints(eqs);
        for (Equipo equipo : eqs) {                    
            aux = buscarHorasSemanales(equipo,aux);                   
        }
        ChartSeries serie1 = new ChartSeries();
        serie1.setLabel(nombreUsuario);
        for (Map.Entry<String, Integer> entry : aux.entrySet()) {
            fechaAux = entry.getKey().substring(8,10)+ "/" + entry.getKey().substring(5,7) + "/" + entry.getKey().substring(0, 4);
            //fechaAux += "-"+ entry.getKey().substring(19,21)+ "/" + entry.getKey().substring(16,18) + "/" + entry.getKey().substring(11, 15);
            serie1.set(fechaAux, entry.getValue());
        }                
        return serie1;
    }
    
    public ChartSeries buscarDatosProfesor(Asignatura asig, List<Usuario> usuarios){
        String fechaAux;
        Map<String,Integer> aux = buscarSemanasAsignatura(asig);
        for (Usuario usr : usuarios) {
            List<Equipo> eqs = equipoFacade.buscarPorUsuarioyAsignatura(usr, asig);
            for (Equipo equipo : eqs) {                    
                aux = buscarHorasSemanales(equipo,aux);                   
            }
        }
        ChartSeries serie1 = new ChartSeries();
        serie1.setLabel("Promedio");
        for (Map.Entry<String, Integer> entry : aux.entrySet()) {
            fechaAux = entry.getKey().substring(8,10)+ "/" + entry.getKey().substring(5,7) + "/" + entry.getKey().substring(0, 4);
            serie1.set(fechaAux, entry.getValue() / usuarios.size());
        }
        return serie1;
    }
    
    public Map<String,Integer> buscarHorasSemanales(Equipo equipo, Map<String,Integer> valores){
        String valorKey;
        int horas;
        List<Semana> semanasSprint = buscarSemanas(equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getFechaInicioSprintAsignatura(),
                equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getFechaTerminoSprintAsignatura());
        List<Miembro> miembros = miembroFacade.buscarPoIdTableroYIdCuenta(equipo.getIdTablero(), equipo.getIdCuenta());
        if(!miembros.isEmpty() && miembros.size() == 1 ){           
            List<DetalleUsuarioTarjeta> detallesT = detalleUsuarioTarjetaFacade.buscarPorIdMiembroYIdTablero(miembros.get(0), equipo.getIdTablero());
            for (DetalleUsuarioTarjeta detalleT : detallesT) {
                for(Semana semana: semanasSprint){
                    valorKey = formatFechaMap(semana.getInicio())+"-"+formatFechaMap(semana.getTermino());
                    switch (detalleT.getIdTarjeta().getIdEstadoTarjeta().getNombreEstadoTarjeta()) {
                        case "En proceso":
                            if((detalleT.getIdTarjeta().getFechaInicioTarjeta().equals(semana.getInicio())
                                    || detalleT.getIdTarjeta().getFechaInicioTarjeta().after(semana.getInicio()))
                                && (detalleT.getIdTarjeta().getFechaInicioTarjeta().before(semana.getTermino()))){
                                
                                if(semana.getTermino().before(new Date())){
                                    horas = obtenerHorasDiff(detalleT.getIdTarjeta().getFechaInicioTarjeta(), semana.getTermino());
                                    if(valores.containsKey(valorKey)) valores.put(valorKey, valores.get(valorKey) + horas);
                                    else valores.put(valorKey, horas);                                    
                                }else{
                                    horas = obtenerHorasDiff(detalleT.getIdTarjeta().getFechaInicioTarjeta(), new Date());
                                    if(valores.containsKey(valorKey)) valores.put(valorKey, valores.get(valorKey) + horas);
                                    else valores.put(valorKey, horas);
                                }
                                
                            }
                            break;
                        case "Terminada":
                            
                            if(detalleT.getIdTarjeta().getFechaFinalTarjeta().equals(semana.getInicio()) 
                                    || detalleT.getIdTarjeta().getFechaFinalTarjeta().after(semana.getInicio())){
                                
                            
                                if(detalleT.getIdTarjeta().getFechaInicioTarjeta().equals(semana.getInicio())
                                        || detalleT.getIdTarjeta().getFechaInicioTarjeta().after(semana.getInicio())){
                                    
                                    if(detalleT.getIdTarjeta().getFechaInicioTarjeta().equals(semana.getTermino())
                                        || detalleT.getIdTarjeta().getFechaInicioTarjeta().before(semana.getTermino())){
                                    
                                        if(detalleT.getIdTarjeta().getFechaFinalTarjeta().equals(semana.getTermino())
                                                || detalleT.getIdTarjeta().getFechaFinalTarjeta().before(semana.getTermino())){
                                            horas = obtenerHorasDiff(detalleT.getIdTarjeta().getFechaInicioTarjeta(), detalleT.getIdTarjeta().getFechaFinalTarjeta());
                                            if(valores.containsKey(valorKey)) valores.put(valorKey, valores.get(valorKey) + horas);
                                            else valores.put(valorKey, horas);   
                                        }else{
                                            horas = obtenerHorasDiff(detalleT.getIdTarjeta().getFechaInicioTarjeta(), semana.getTermino());
                                            if(valores.containsKey(valorKey)) valores.put(valorKey, valores.get(valorKey) + horas);
                                            else valores.put(valorKey, horas); 
                                        }
                                    }                                                                      
                                }else{
                                   if(detalleT.getIdTarjeta().getFechaFinalTarjeta().equals(semana.getTermino())
                                            || detalleT.getIdTarjeta().getFechaFinalTarjeta().before(semana.getTermino())){
                                        horas = obtenerHorasDiff(semana.getInicio(), detalleT.getIdTarjeta().getFechaFinalTarjeta());
                                        if(valores.containsKey(valorKey)) valores.put(valorKey, valores.get(valorKey) + horas);
                                        else valores.put(valorKey, horas); 
                                    }else{
                                        horas = obtenerHorasDiff(semana.getInicio(), semana.getTermino());
                                        if(valores.containsKey(valorKey)) valores.put(valorKey, valores.get(valorKey) + horas);
                                        else valores.put(valorKey, horas); 
                                    }
                                }
                            } 
                            break;
                    }
                }
            }
        }
        return valores;
    }
    
    public Map<String,Integer> buscarSemanasSprints(List<Equipo> eqs){
        List<SprintAsignatura> sprintsA = new ArrayList<>();
        Map<String,Integer> valores = new TreeMap<>();
        String valorKey;
        for (Equipo equipo : eqs) {
            sprintsA.add(equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura());
        }
        Collections.sort(sprintsA);
        List<Semana> semanas = new ArrayList<>();
        if(!sprintsA.isEmpty()){
            semanas = buscarSemanas(sprintsA.get(0).getFechaInicioSprintAsignatura()
                , sprintsA.get(sprintsA.size()-1).getFechaTerminoSprintAsignatura()); 
        }
        for(Semana semana: semanas){
            valorKey = formatFechaMap(semana.getInicio())+"-"+formatFechaMap(semana.getTermino());
            valores.put(valorKey,0);
        }
        return valores;
    }
    
    public Map<String,Integer> buscarSemanasAsignatura(Asignatura asig){
        Map<String,Integer> valores = new TreeMap<>();
        String valorKey;
        List<SprintAsignatura> sprintsA = sprintAsignaturaFacade.buscarPorAsignatura(asig);
        Collections.sort(sprintsA);
        List<Semana> semanas = new ArrayList<>();
        if(!sprintsA.isEmpty()){
            semanas = buscarSemanas(sprintsA.get(0).getFechaInicioSprintAsignatura()
                , sprintsA.get(sprintsA.size()-1).getFechaTerminoSprintAsignatura()); 
        }
        for(Semana semana: semanas){
            valorKey = formatFechaMap(semana.getInicio())+"-"+formatFechaMap(semana.getTermino());
            valores.put(valorKey,0);
        }
        return valores;
    }
    
    public List<Semana> buscarSemanas(Date datei, Date datef){
        DateTime fechai = new DateTime(datei);
        DateTime fechaf = new DateTime(datef);
        LocalDate lunesI = fechai.toLocalDate().withDayOfWeek(DateTimeConstants.MONDAY);
        LocalDate domingoF = fechaf.toLocalDate().withDayOfWeek(DateTimeConstants.SUNDAY);
        
        List<Semana> semanas = new ArrayList<>();
        DateTime i = lunesI.toDateTimeAtStartOfDay();
        while(i.isBefore(domingoF.toDateTimeAtStartOfDay())){            
            DateTime domingoi = i.toLocalDate().withDayOfWeek(DateTimeConstants.SUNDAY).toDateTimeAtStartOfDay();
            domingoi = domingoi.plusHours(23).plusMinutes(59).plusSeconds(59);
            semanas.add(new Semana(i.toDate(),domingoi.toDate()));
            i = i.plusWeeks(1);
        }
        return semanas;
    }
    
    public int obtenerHorasDiff(Date inicio, Date fin){
        DateTime dtI = new DateTime(inicio);
        DateTime dtF = new DateTime(fin);        
        int tiempo = Hours.hoursBetween(dtI, dtF).getHours(); 
        return tiempo;
    }
    
    public String formatFecha(Date fecha){
        if(fecha == null) return "-";        
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String datef = df.format(fecha);
        return datef;
    }
    
    public String formatFechaMap(Date fecha){
        if(fecha == null) return "-";        
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String datef = df.format(fecha);
        return datef;
    }
    
}
