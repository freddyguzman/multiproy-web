/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Equipo;
import cl.usach.entities.Tarjeta;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.TarjetaFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;

/**
 *
 * @author FGT
 */
@Named(value = "graficoManagedBean")
@ViewScoped
public class GraficoManagedBean {
    @EJB
    private TarjetaFacadeLocal tarjetaFacade;
    @EJB
    private EquipoFacadeLocal equipoFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;

    private List<Equipo> equipos; 
    
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
    public GraficoManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        buscarProjectos();
    }
    
    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }
   
    public void buscarProjectos(){
        Usuario usuario = usuarioFacade.buscarPorLogin(loginUsuario);
        equipos = equipoFacade.buscarPorUsuario(usuario);
    }
    
    public LineChartModel crearGraficos(Equipo equipo){
        LineChartModel lineModelAux = buscarDatosGraficos(equipo);
        lineModelAux.setTitle(equipo.getIdTablero().getNombreTablero());
        lineModelAux.setLegendPosition("e");
        lineModelAux.setShowPointLabels(true);
        lineModelAux.getAxes().put(AxisType.X, new CategoryAxis("Días"));
        Axis yAxis = lineModelAux.getAxis(AxisType.Y);
        yAxis.setLabel("Tareas");
        yAxis.setMin(0);
        return lineModelAux;
    }
    
    public LineChartModel buscarDatosGraficos(Equipo equipo){
        List<Tarjeta> tarjetas = tarjetaFacade.buscarPorTablero(equipo.getIdTablero());
        LineChartModel model = new LineChartModel();
        Collections.sort(tarjetas);
        Map<String,Number> valores;
        if(equipo.getIdTablero().getIdSprintGrupo() != null){
            if(equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getFechaTerminoSprintAsignatura().after(new Date())){
                valores = inicializarMAP(equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getFechaInicioSprintAsignatura(),
                new Date());
            }else{
                valores = inicializarMAP(equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getFechaInicioSprintAsignatura(),
                    equipo.getIdTablero().getIdSprintGrupo().getIdSprintAsignatura().getFechaTerminoSprintAsignatura());
            }           
        }else{
            valores = inicializarMAP(tarjetas.get(0).getFechaCreacionTarjeta(), new Date());
        }
        
        if(valores != null){
            for (Tarjeta tarjeta : tarjetas) {           
                for (Map.Entry<String, Number> valor : valores.entrySet()) {                
                    if(verificarSiCuenta(valor.getKey(), tarjeta.getFechaCreacionTarjeta(), tarjeta.getFechaFinalTarjeta())){
                        int i = valores.get(valor.getKey()).intValue() + 1;
                        valores.put(valor.getKey(), i);
                    }                
                }
            }
            Map<String,Number> valoresGraf = new TreeMap<>(valores);
            String fechaAux;

            ChartSeries serie1 = new ChartSeries();
            serie1.setLabel("Cantidad");
            ChartSeries serie2 = new ChartSeries();
            serie2.setLabel("Ideal");
            int i = 0;
            Boolean flag = true;
            for (Map.Entry<String, Number> valorG : valoresGraf.entrySet()) {
                fechaAux = valorG.getKey().substring(8,10)+ "-" + valorG.getKey().substring(5,7) + "-" + valorG.getKey().substring(0, 4);
                serie1.set(fechaAux, valorG.getValue().intValue());
                
                if(i == 0 ){
                    if(valorG.getValue().intValue() != 0){
                        serie2.set(fechaAux, valorG.getValue().intValue());
                        flag = false;
                    }
                }else if(flag && valorG.getValue().intValue() > 0){
                    serie2.set(fechaAux, valorG.getValue().intValue());
                    flag = false;
                }
                else if (i == (valoresGraf.size()-1)) {
                    serie2.set(fechaAux, 0);
                }
                
                i++;
            }
            
            model.addSeries(serie1);
            model.addSeries(serie2);
        }
        return model;
    }
    
    public Map<String,Number> inicializarMAP(Date fechai, Date fechaf){
        Map<String,Number> valores = new HashMap<>();
        try {
            fechai = formatter.parse(formatter.format(fechai));
            fechaf = formatter.parse(formatter.format(fechaf));
            
            DateTime datei = new DateTime(fechai);
            DateTime datef = new DateTime(fechaf);

            int dias = Days.daysBetween(datei, datef).getDays();
            for(int i = 0; i <= dias; i++){
                DateTime d = datei.withFieldAdded(DurationFieldType.days(), i);            
                valores.put(formatter.format(d.toDate()), 0);
            }            
            return valores;
            
        } catch (ParseException ex) {
            Logger.getLogger(GraficoManagedBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }       
    }
    
    public Boolean verificarSiCuenta(String fechaCheck, Date fechaCreacion, Date fechaFinal){
        Date fechaChk;
        
        if(fechaFinal != null){
            String date1 = formatter.format(fechaCreacion);
            String date2 = formatter.format(fechaFinal);

            try {
                fechaChk = formatter.parse(fechaCheck);
                fechaCreacion = formatter.parse(date1);
                fechaFinal = formatter.parse(date2);
                                
                if((fechaChk.after(fechaCreacion) || fechaChk.equals(fechaCreacion))
                        && (fechaChk.before(fechaFinal))){
                    return true;
                }
            } catch (ParseException ex) {
                Logger.getLogger(GraficoManagedBean.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }else{
            String date1 = formatter.format(fechaCreacion);

            try {
                fechaChk = formatter.parse(fechaCheck);
                fechaCreacion = formatter.parse(date1);
                
                if((fechaChk.after(fechaCreacion) || fechaChk.equals(fechaCreacion))){
                    return true;
                }
            } catch (ParseException ex) {
                Logger.getLogger(GraficoManagedBean.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        
        return false;
    }
    
}
