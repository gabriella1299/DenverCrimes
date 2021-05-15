package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	private SimpleWeightedGraph<String,DefaultWeightedEdge> grafo;
	private EventsDao dao;
	public List<String> percorsoMigliore;
	
	public Model() {
		dao=new EventsDao();
	}
	
	public List<String> getCategorie(){
		return dao.getCategorie();
	}
	
	public void creaGrafo(String categoria, int mese) {
		this.grafo=new SimpleWeightedGraph<String,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//Aggiunta vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
		
		//Aggiunta archi
		//Reato A e B devono essere avvenuti nello stesso quartiere e il numero di volte in cui sono avvenuti
		for(Adiacenza a:dao.getAdiacenze(categoria, mese)) {
			if(this.grafo.getEdge(a.getV1(), a.getV2())==null) {//se non c'e' ancora l'arco lo aggiungo; noi abbiamo messo nella query il > quindi e' superfluo
				Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(),a.getPeso());
			}
		}
		
		System.out.println("#Vertici: "+this.grafo.vertexSet().size());
		System.out.println("#Archi: "+this.grafo.edgeSet().size());
		
	}
	
	public List<Adiacenza> getArchi(){
		//Calcolo il peso medio degli archi presenti nel grafo
		double pesoMedio=0.0;
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			pesoMedio+=this.grafo.getEdgeWeight(e);
		}
		pesoMedio=pesoMedio/this.grafo.edgeSet().size();
		
		//'filtro' gli archi, tenendo solo quelli che hanno peso > del peso medio
		List<Adiacenza> result=new LinkedList<>();
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>pesoMedio) {
				result.add(new Adiacenza(this.grafo.getEdgeSource(e),this.grafo.getEdgeTarget(e),this.grafo.getEdgeWeight(e)));
			}
			
		}
		return result;
	}
	
	public List<String> trovaPercorso(String sorgente,String destinazione){
		//vogliamo trovare percorso piu' lungo: problema di ottimizzazione
		this.percorsoMigliore=new ArrayList<>(); //non possiamo fare =null perche sotto richiamo .size e su un valore null scatena nullPointer!
		List<String> parziale=new ArrayList<>();
		
		parziale.add(sorgente);
		
		cerca(destinazione,parziale);
		
		return this.percorsoMigliore;
	}
	
	private void cerca(String destinazione,List<String> parziale) {
		//CASO TERMINALE: quando siamo arrivati a destinazione, ultimo elemento di parziale coincide con destinazione
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			//controlliamo se il percorso sia migliore: > numero di vertici visitati
			if(parziale.size()>this.percorsoMigliore.size()) {
				this.percorsoMigliore=new LinkedList<>(parziale);
				return;
			}
		}
		
		//Scorro i vicini dell'ultimo inserito e provo ad aggiungerli uno ad uno
		//Altrimenti aggiungo nuovi vertici per proseguire il percorso
		//ciclo su vertici adiacenti, a chi? Dell'ultimo inserito. Provo ad aggiungerli uno per uno
		for(String vicino: Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(vicino)) {
				parziale.add(vicino);
				cerca(destinazione,parziale);
				//BACKTRACKING
				parziale.remove(parziale.size()-1);//tolgo ultimo inserito
			}
		}
		
		
		
	}
}
