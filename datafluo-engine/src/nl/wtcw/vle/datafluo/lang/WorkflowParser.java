package nl.wtcw.vle.datafluo.lang;



import nl.wtcw.vle.datafluo.core.flow.Flow;


public interface WorkflowParser<T> {
  Flow parse(T topology) throws ParsingException;
}
