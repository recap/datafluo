/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.html;

/**
 *
 * @author reggie
 */
/*<tr>
<td rowspan="2" bgcolor=#0000ff>Q197F8 vs O55735</td>
<td>ILEKGKLTITNLMKSLGFKPKPKK-IQSID</td>
</tr>
<tr>
<td>VLECTHVLCSNCVKKINVCPICRKTFQSIN</td>
</tr>*/

public class HtmlRender {
	
	public String HtmlPrint(String print){
		String result = "<tr>";
		String lines[] = print.split("[\\r\\n]+");
		for(int i = 1; i < lines.length; i++){
			if(i == 1){
				String row = "<td rowspan=\"2\" bgcolor=#0000ff>"+lines[i]+"</td>\n";
				result = result.concat(row);
				String row1 = "<td>"+lines[i+1]+"</td></tr>\n";
				result = result.concat(row1);
				i++;
			}else{
				String row = "<tr><td>"+lines[i]+"</td></tr>\n";
				result = result.concat(row);
			}
		}
		//result = result.concat("</tr>");
		return result;
	}

}
