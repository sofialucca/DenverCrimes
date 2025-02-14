package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	//vertici sono i tipi di reato
		//--> stringhe tale da non richiedere identity Map
	private SimpleWeightedGraph<String,DefaultWeightedEdge> grafo;
	private EventsDao dao;
	private List<String> percorsoMigliore;
	
	public Model() {
		dao = new EventsDao();
	}
	
	public void creaGrafo(String categoria, int mese) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//agiunta vertici
		Graphs.addAllVertices(grafo, dao.getVertici(categoria, mese));
		
		//agiunta archi
		for(Adiacenza a : dao.getArchi(categoria, mese)) {
			if(this.grafo.getEdge(a.getV1(), a.getV2()) == null) {
				Graphs.addEdge(grafo, a.getV1(), a.getV2(), a.getPeso());
			}
		}
		
		System.out.println("Grafo creato con successo: "+grafo.vertexSet().size()+"vertici e "+grafo.edgeSet().size()+" archi");
	}
	
	public List<Adiacenza> getArchi(){
		// calcolo peso medio arhi presenti nel grafo
		double pesoMedio = 0;
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			pesoMedio += this.grafo.getEdgeWeight(e);
		}
		pesoMedio = pesoMedio/this.grafo.edgeSet().size();
		//filtro gli archi maggiori del peso medio
		List<Adiacenza> result = new LinkedList<>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>pesoMedio) {
				result.add(new Adiacenza(this.grafo.getEdgeSource(e),this.grafo.getEdgeTarget(e),this.grafo.getEdgeWeight(e)));
			}
		}
		
		return result;
	}
	
	public List<String> trovaPercorso(String sorgente, String destinazione) {
		this.percorsoMigliore = new ArrayList<>();
		List<String> parziale = new ArrayList<>();
		parziale.add(sorgente);
		cerca(destinazione, parziale);
		return percorsoMigliore;
	}
	
	private void cerca(String destinazione, List<String> parziale) {
		//caso terminale
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			if(parziale.size()>this.percorsoMigliore.size()) {
				percorsoMigliore = new ArrayList<>(parziale);
			}
			
			return;
		}
		
		for(String s: Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(s)) {
				parziale.add(s);
				cerca(destinazione, parziale);
				parziale.remove(parziale.size()-1);
			}
		}
	}
	
	public List<String> getCategorie(){
		return dao.getCategorie();
	}
}
