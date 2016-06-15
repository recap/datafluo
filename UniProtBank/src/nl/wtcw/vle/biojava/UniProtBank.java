/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.biojava;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.alignment.SimpleGapPenalty;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.template.Profile;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.io.FastaReaderHelper;
import org.biojava3.core.util.ConcurrencyTools;

/**
 *
 * @author reggie
 */
public class UniProtBank {

	private ProteinSequence getSequenceForId(String uniProtId) throws Exception {
        URL uniprotFasta = new URL(String.format("http://www.uniprot.org/uniprot/%s.fasta", uniProtId));
        ProteinSequence seq = FastaReaderHelper.readFastaProteinSequence(uniprotFasta.openStream()).get(uniProtId);
        //System.out.printf("id : %s %s%n%s%n", uniProtId, seq, seq.getOriginalHeader());
		seq.getSequenceAsString();

        return seq;
    }

	private String getStringSequenceForId(String uniProtId) throws Exception {
        URL uniprotFasta = new URL(String.format("http://www.uniprot.org/uniprot/%s.fasta", uniProtId));
        ProteinSequence seq = FastaReaderHelper.readFastaProteinSequence(uniprotFasta.openStream()).get(uniProtId);
        //System.out.printf("id : %s %s%n%s%n", uniProtId, seq, seq.getOriginalHeader());
        return seq.getOriginalHeader() +"\n"+seq.getSequenceAsString();
    }

	private String[] getSequenceForId(String[] ids) throws Exception{
		List<String> lst = new ArrayList<String>();
		for(int i = 0; i < ids.length; i++){
			URL uniprotFasta = new URL(String.format("http://www.uniprot.org/uniprot/%s.fasta", ids[i]));
			//System.out.println("URL: "+uniprotFasta.toString());
			BufferedReader in = new BufferedReader(	new InputStreamReader(uniprotFasta.openStream()));
			String contents = new String();
			String line;
			while ((line = in.readLine()) != null)
					contents = contents.concat(line+"\n");
			in.close();
			lst.add(contents);
			//System.out.println("CONTENTS: " +contents);
		}
		

		
		String[] result = (String[])lst.toArray(new String[lst.size()]);
		return result;
	}

	private String pairSequenceAlignment2(String sequence1, String sequence2) throws Exception {
        List<ProteinSequence> lst = new ArrayList<ProteinSequence>();
		ByteArrayInputStream seqStream1 = new ByteArrayInputStream(sequence1.getBytes());
		ByteArrayInputStream seqStream2 = new ByteArrayInputStream(sequence2.getBytes());
		LinkedHashMap<String, ProteinSequence> a  = FastaReaderHelper.readFastaProteinSequence(seqStream1);
		LinkedHashMap<String, ProteinSequence> b  = FastaReaderHelper.readFastaProteinSequence(seqStream2);
		ProteinSequence protSeq1 = null;
		ProteinSequence protSeq2 = null;
		for (  Entry<String, ProteinSequence> entry : a.entrySet() ) {
			protSeq1 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		for (  Entry<String, ProteinSequence> entry : b.entrySet() ) {
			protSeq2 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		SubstitutionMatrix<AminoAcidCompound> matrix = new SimpleSubstitutionMatrix<AminoAcidCompound>();
        SequencePair<ProteinSequence, AminoAcidCompound> pair = Alignments.getPairwiseAlignment(protSeq1, protSeq2,
                PairwiseSequenceAlignerType.GLOBAL, new SimpleGapPenalty(), matrix);

		String result = String.format("%n%s%s vs %s%n%s", "GLOBAL: ", pair.getQuery().getAccession(), pair.getTarget().getAccession(), pair);

		return result;


		/*lst.add(protSeq1);
		lst.add(protSeq2);

        Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);

		return profile.toString();*/
        //System.out.printf("Clustalw:%n%s%n", profile);
        //ConcurrencyTools.shutdown();
    }

	private String pairSequenceAlignment(String[] sequence) throws Exception {
		if(sequence.length < 2)
			return "Expected Sequence List Size 2 But Got " + Integer.toString(sequence.length)+"\n";

        List<ProteinSequence> lst = new ArrayList<ProteinSequence>();
		ByteArrayInputStream seqStream1 = new ByteArrayInputStream(sequence[0].getBytes());
		ByteArrayInputStream seqStream2 = new ByteArrayInputStream(sequence[1].getBytes());
		LinkedHashMap<String, ProteinSequence> a  = FastaReaderHelper.readFastaProteinSequence(seqStream1);
		LinkedHashMap<String, ProteinSequence> b  = FastaReaderHelper.readFastaProteinSequence(seqStream2);
		ProteinSequence protSeq1 = null;
		ProteinSequence protSeq2 = null;
		for (  Entry<String, ProteinSequence> entry : a.entrySet() ) {
			protSeq1 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		for (  Entry<String, ProteinSequence> entry : b.entrySet() ) {
			protSeq2 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		SubstitutionMatrix<AminoAcidCompound> matrix = new SimpleSubstitutionMatrix<AminoAcidCompound>();
        SequencePair<ProteinSequence, AminoAcidCompound> pair = Alignments.getPairwiseAlignment(protSeq1, protSeq2,
                PairwiseSequenceAlignerType.LOCAL, new SimpleGapPenalty(), matrix);

		String result = String.format("%n%s%s vs %s%n%s", "LOCAL: ", pair.getQuery().getAccession(), pair.getTarget().getAccession(), pair);

		return result;


		/*lst.add(protSeq1);
		lst.add(protSeq2);

        Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);

		return profile.toString();*/
        //System.out.printf("Clustalw:%n%s%n", profile);
        //ConcurrencyTools.shutdown();
    }


	public String pairSequenceAlignmentGlobal(String[] sequence) throws Exception {
		if(sequence.length < 2)
			return "Expected Sequence List Size 2 But Got " + Integer.toString(sequence.length)+"\n";

        List<ProteinSequence> lst = new ArrayList<ProteinSequence>();
		ByteArrayInputStream seqStream1 = new ByteArrayInputStream(sequence[0].getBytes());
		ByteArrayInputStream seqStream2 = new ByteArrayInputStream(sequence[1].getBytes());
		LinkedHashMap<String, ProteinSequence> a  = FastaReaderHelper.readFastaProteinSequence(seqStream1);
		LinkedHashMap<String, ProteinSequence> b  = FastaReaderHelper.readFastaProteinSequence(seqStream2);
		ProteinSequence protSeq1 = null;
		ProteinSequence protSeq2 = null;
		for (  Entry<String, ProteinSequence> entry : a.entrySet() ) {
			protSeq1 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		for (  Entry<String, ProteinSequence> entry : b.entrySet() ) {
			protSeq2 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		SubstitutionMatrix<AminoAcidCompound> matrix = new SimpleSubstitutionMatrix<AminoAcidCompound>();
        SequencePair<ProteinSequence, AminoAcidCompound> pair = Alignments.getPairwiseAlignment(protSeq1, protSeq2,
                PairwiseSequenceAlignerType.GLOBAL, new SimpleGapPenalty(), matrix);

		String result = String.format("%n%s%s vs %s%n%s", "GLOBAL: ", pair.getQuery().getAccession(), pair.getTarget().getAccession(), pair);

		return result;


		/*lst.add(protSeq1);
		lst.add(protSeq2);

        Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);

		return profile.toString();*/
        //System.out.printf("Clustalw:%n%s%n", profile);
        //ConcurrencyTools.shutdown();
    }

	private String testArray(String[] a){
		 
		 String globals = new String();
		 for(int i =0; i < a.length; i++){
			 System.out.println("testArray OUTPUT: "+a[i]);
			 globals = globals.concat(a[i]);
			 globals = globals.concat("\n");
		 }
		 return globals;
	 }


	private String alignPairGlobal(String id1, String id2, int sl) throws Exception {
       //ProteinSequence s1 = getSequenceForId(id1), s2 = getSequenceForId(id2);
       // SubstitutionMatrix<AminoAcidCompound> matrix = new SimpleSubstitutionMatrix<AminoAcidCompound>();
      //  SequencePair<ProteinSequence, AminoAcidCompound> pair = Alignments.getPairwiseAlignment(s1, s2,
      //          PairwiseSequenceAlignerType.GLOBAL, new SimpleGapPenalty(), matrix);
		//String ret = String.format("%n%s vs %s%n%s", pair.getQuery().getAccession(), pair.getTarget().getAccession(), pair);
		Thread.sleep(sl);
		String ret = "DONE";

		return ret;
    }

	private String Test(String a){
		return "OK";
	}

}
