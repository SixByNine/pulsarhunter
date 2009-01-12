/*
Copyright (C) 2005-2007 Michael Keith, University Of Manchester

email: mkeith@pulsarastronomy.net
www  : www.pulsarastronomy.net/wiki/Software/PulsarHunter

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

 */
/*
 * DatabaseDataLibrary.java
 *
 * Created on 09 January 2007, 11:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.jreaper;

import coordlib.Coordinate;
import coordlib.CoordinateDistanceComparitorGalactic;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.jreaper.gui.LoadingSplash;
import pulsarhunter.jreaper.pmsurv.PulsarCandFile;

/**
 *<p>
 *  DatabaseDataLibrary extends the DataLibarary with functionality
 *  that alows for detection lists (i.e. 'class1' files) to be stored
 *  in a SQL database.
 *</p><p>
 *  The database must currently be created externaly and must be created in
 *  a form compatable to the SQL dump below:
 *</p><p>
 *  <code>
 * -- <br />
 * -- Table structure for table `candidates`<br />
 * -- <br />
 *<br />
 * CREATE TABLE `candidates` (<br />
 *  `ID` int(11) NOT NULL auto_increment,<br />
 *  `Class` smallint(6) NOT NULL default '0',<br />
 *  `BeamID` varchar(18) NOT NULL default '',<br />
 *  `GridID` int(11) NOT NULL default '0',<br />
 *  `RA` double NOT NULL default '0',<br />
 *  `Dec` double NOT NULL default '0',<br />
 *  `RAStr` varchar(10) NOT NULL default '',<br />
 *  `DecStr` varchar(10) NOT NULL default '',<br />
 *  `Gl` float NOT NULL default '0',<br />
 *  `Gb` float NOT NULL default '0',<br />
 *  `Period` float NOT NULL default '0',<br />
 *  `DM` float NOT NULL default '0',<br />
 *  `Accn` float NOT NULL default '0',<br />
 *  `Jerk` float NOT NULL default '0',<br />
 *  `SNR_SPEC` float NOT NULL default '0',<br />
 *  `SNR_RECON` float NOT NULL default '0',<br />
 *  `SNR_FOLD` float NOT NULL default '0',<br />
 *  `MJD` float NOT NULL default '0',<br />
 *  `DateAdded` date NOT NULL default '0000-00-00',<br />
 *  `Status` varchar(25) NOT NULL default '',<br />
 *  `Comment` text NOT NULL,<br />
 *  `DataType` varchar(8) NOT NULL default '',<br />
 *  `ProcType` varchar(8) NOT NULL default '',<br />
 *  `TSamp` int(11) NOT NULL default '0',<br />
 *  `HarmFold` varchar(8) NOT NULL default '',<br />
 *  `Redetection` int(11) NOT NULL default '0',<br />
 *  `numdetections` int(11) NOT NULL default '0',<br />
 *  `DuplicateTest` varchar(50) NOT NULL default '',<br />
 *  PRIMARY KEY  (`ID`),<br />
 *  FULLTEXT KEY `Comment` (`Comment`)<br />
 *  ) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;<br />
 *</code>
 *</p>
 *
 * @author mkeith
 */
public class DatabaseDataLibrary extends DataLibrary implements WebDataLibrary {

    private SQLDatabaseConnectionInfo databaseConnection = new SQLDatabaseConnectionInfo("", "", "");
    private boolean newDetection = false;
    private String externalWebRootUrl;
    private String internalWebRootPath;

    /** Creates a new instance of DatabaseDataLibrary */
    public DatabaseDataLibrary(File rootPath, SQLDatabaseConnectionInfo databaseConnection, String internalWebRootPath, String externalWebRootUrl) {
        super(rootPath);
        this.databaseConnection = databaseConnection;
        this.externalWebRootUrl = externalWebRootUrl;
        this.internalWebRootPath = internalWebRootPath;
        new File(internalWebRootPath).mkdir();
        new File(internalWebRootPath + File.separator + "plots").mkdir();

    }

    public DatabaseDataLibrary() throws IOException {
        super();
    }

    public void webUpdate(Cand cand) {


        if (cand.getCandClass() < -1 || cand.getCandClass() > 3) {
            return;
        }
        int id = -1;

        try {

            Statement stmt;
            Connection con;
            con = DriverManager.getConnection(databaseConnection.getUrl(), databaseConnection.getUsername(), databaseConnection.getPassword());
            stmt = con.createStatement();



            ResultSet rs = stmt.executeQuery("SELECT * FROM `candidates` WHERE `DuplicateTest`='" + cand.getUniqueIdentifier() + "'");

            ArrayList<SQLpair> sqldata = new ArrayList<SQLpair>();
            boolean update = false;
            if (rs.next()) {
                update = true;

                sqldata.add(new SQLpair("Class", String.valueOf(cand.getCandClass())));

            } else {
                update = false;

                sqldata.add(new SQLpair("Class", String.valueOf(cand.getCandClass())));
                sqldata.add(new SQLpair("Name", String.valueOf(cand.getName())));
                sqldata.add(new SQLpair("BeamID", String.valueOf(cand.getBeam().getName())));
                if (cand.getCandidateFile() instanceof PulsarCandFile) {
                    sqldata.add(new SQLpair("GridID", String.valueOf(((PulsarCandFile) cand.getCandidateFile()).getGridID())));
                } else if (cand.getCandidateFile() instanceof PulsarHunterCandidate) {
                    sqldata.add(new SQLpair("GridID", String.valueOf(((PulsarHunterCandidate) cand.getCandidateFile()).getHeader().getSourceID()).substring(1)));
                } else {
                    sqldata.add(new SQLpair("GridID", String.valueOf(1)));
                }
                sqldata.add(new SQLpair("RA", String.valueOf(cand.getBeam().getCoord().getRA().toString(true))));
                sqldata.add(new SQLpair("Dec", String.valueOf(cand.getBeam().getCoord().getDec().toString(true))));
                sqldata.add(new SQLpair("RAStr", String.valueOf(cand.getBeam().getCoord().getRA().toString(false))));
                sqldata.add(new SQLpair("DecStr", String.valueOf(cand.getBeam().getCoord().getDec().toString(false))));
                sqldata.add(new SQLpair("Gl", String.valueOf(cand.getBeam().getCoord().getGl())));
                sqldata.add(new SQLpair("Gb", String.valueOf(cand.getBeam().getCoord().getGb())));
                sqldata.add(new SQLpair("Period", String.valueOf(cand.getPeriod())));
                sqldata.add(new SQLpair("DM", String.valueOf(cand.getDM())));
                sqldata.add(new SQLpair("Accn", String.valueOf(cand.getAccel())));
                sqldata.add(new SQLpair("Jerk", String.valueOf(cand.getJerk())));
                sqldata.add(new SQLpair("SNR_SPEC", String.valueOf(cand.getSpecSNR())));
                sqldata.add(new SQLpair("SNR_RECON", String.valueOf(cand.getReconSNR())));
                sqldata.add(new SQLpair("SNR_FOLD", String.valueOf(cand.getFoldSNR())));
                sqldata.add(new SQLpair("MJD", String.valueOf(cand.getMJD())));
                sqldata.add(new SQLpair("DateAdded", String.valueOf(new java.sql.Date(new java.util.Date().getTime()).toString())));
                if (cand.getCandidateFile() instanceof PulsarHunterCandidate) {
                    PulsarHunterCandidate osrfFile = (PulsarHunterCandidate) cand.getCandidateFile();
                    sqldata.add(new SQLpair("DataType", osrfFile.getHeader().getExtraValueSafe("DataType")));
                    sqldata.add(new SQLpair("ProcType", osrfFile.getHeader().getExtraValueSafe("ProcType")));
                    sqldata.add(new SQLpair("TSamp", String.valueOf(osrfFile.getHeader().getTsamp())));
                    sqldata.add(new SQLpair("HarmFold", osrfFile.getHeader().getExtraValueSafe("ProcType")));
                } else {
                    sqldata.add(new SQLpair("DataType", String.valueOf("")));
                    sqldata.add(new SQLpair("ProcType", String.valueOf("")));
                    sqldata.add(new SQLpair("TSamp", String.valueOf("")));
                    sqldata.add(new SQLpair("HarmFold", String.valueOf("")));
                }
//            sqldata.add(new SQLpair("Redetection",String.valueOf("0")));
//            sqldata.add(new SQLpair("numdetections",String.valueOf("0")));
                sqldata.add(new SQLpair("DuplicateTest", cand.getUniqueIdentifier()));
                sqldata.add(new SQLpair("FileName", cand.getPhfile().getUniqueIdentifier()));
            }
            if (cand.getCandClass() == 0) {
                // kpsrs MUST have status=PSR
                sqldata.add(new SQLpair("Status", "PSR"));
            }

            if (update) {
                System.out.println("Updating database...");

                id = rs.getInt(1);
                String status = rs.getString("Status");
                int redetectionOf = rs.getInt("Redetection");
                int numDet = rs.getInt("numdetections");


                if (redetectionOf != 0) {
                    // should update the name of the master of this set...
                    stmt.execute("UPDATE `candidates` SET `name`='" + cand.getName() + "' Where `ID`=" + redetectionOf + ";");
                }
                if (numDet != 0) {
                    // need to update the name of the decendants of this set...
                    ResultSet rs2 = stmt.executeQuery("SELECT * FROM `candidates` WHERE `Redetection`='" + id + "';");

                    ArrayList<Integer> idList = new ArrayList<Integer>();
                    while (rs2.next()) {
                        int targetID = rs2.getInt("ID");
                        idList.add(targetID);
                    }

                    for (int targetID : idList) {
                        stmt.execute("UPDATE `candidates` SET `Name`='" + cand.getName() + "' Where `ID`=" + targetID + ";");

                    }


                }


                if (cand.getCandClass() != 0 && status.equals("PSR")) {
                    // Candidates that are not class 0 cannot have status PSR, so make em NEW
                    sqldata.add(new SQLpair("Status", "NEW"));
                }

                boolean first = true;
                StringBuffer buf = new StringBuffer();
                for (SQLpair data : sqldata) {
                    if (!first) {
                        buf.append(", ");
                    }
                    buf.append(data.getAsUpdate());
                    first = false;
                }



                stmt.execute("UPDATE `candidates` SET " + buf.toString() + " Where `ID`=" + id + ";");


            } else {
                System.out.println("Inserting into database...");
                if (cand.getCandClass() == 0) {
// no need to set the status anymore! It is done always for kpsrs!
                } else {
                    sqldata.add(new SQLpair("Status", "NEW"));
                }

                StringBuffer bufV = new StringBuffer();
                StringBuffer bufK = new StringBuffer();
                boolean first = true;
                for (SQLpair data : sqldata) {
                    if (!first) {
                        bufK.append(", ");
                        bufV.append(",");
                    }
                    bufK.append("`" + data.getAsInsertKey() + "`");

                    bufV.append("'" + data.getAsInsertValue() + "'");
                    first = false;
                }
//                System.out.println("INSERT INTO `candidates` ("+bufK.toString()+") VALUES ("+bufV.toString()+");");
                stmt.execute("INSERT INTO `candidates` (" + bufK.toString() + ") VALUES (" + bufV.toString() + ");", Statement.RETURN_GENERATED_KEYS);
                rs = stmt.getGeneratedKeys();


                if (rs.next()) {
                    id = rs.getInt(1);
                }
                newDetection = true;
            }


            /* Need to find redetections and stuff */
            stmt.close();

            con.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        /**
         *  Make Plot!
         *
         */
        File createdFile = new File(internalWebRootPath + File.separator + "plots" + File.separator + cand.getCandidateFile().getFile().getName() + ".png");
//        System.out.println("Checking "+createdFile.getPath());
        if (!createdFile.exists()) {
            createdFile = cand.getCandidateFile().createImage(cand, internalWebRootPath + File.separator + "plots");
            System.out.println("Created " + createdFile.getPath());


            if (createdFile != null) {
                try {
                    // if it's null there is no plot, so we can't do anything about it right now.

                    Statement stmt;
                    Connection con;

                    con =
                            DriverManager.getConnection(databaseConnection.getUrl(), databaseConnection.getUsername(), databaseConnection.getPassword());
                    stmt =
                            con.createStatement();


                    ResultSet rs = stmt.executeQuery("SELECT * FROM `images` WHERE `candid`=" + id + " AND `filename`='plots/" + createdFile.getName() + "'");
                    if (!rs.next()) {
                        stmt.execute("INSERT INTO `images` (`candid`,`filename`,`description`) VALUES (" + id + ", 'plots/" + createdFile.getName() + "','JReaper Plot');");
                    }

                    stmt.close();
                    con.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
        }
        cand.getPhfile().release();

    }

    public void webSync(CandList cl) {
//        final LoadingSplash lsplash = new LoadingSplash();
        try {


            Statement stmt;
            Connection con;

            con =
                    DriverManager.getConnection(databaseConnection.getUrl(), databaseConnection.getUsername(), databaseConnection.getPassword());
            stmt =
                    con.createStatement();



            ResultSet rs = stmt.executeQuery("SELECT * FROM `candidates` WHERE `Redetection`=0 AND `Class` <> 0;");
//            java.awt.EventQueue.invokeLater(new Runnable() {
//
//                public void run() {
//                    lsplash.setVisible(true);
//                    lsplash.setText("Synching with database");
//                    lsplash.repaint();
//                }
//            });
            CoordinateDistanceComparitorGalactic comp = new CoordinateDistanceComparitorGalactic(0, 0);

            while (rs.next()) {

                final String name = rs.getString("Name");
                int candClass = rs.getInt("Class");
                double period = rs.getDouble("Period");
                double dm = rs.getDouble("Dm");
                double gl = rs.getDouble("Gl");
                double gb = rs.getDouble("Gb");
                String status = rs.getString("Status");

                if (comp.difference(gl, gb, cl.getBeam().getCoord().getGl(), cl.getBeam().getCoord().getGb()) < this.getRefiner().getDistmax()) {

//                    Coordinate coord = new Coordinate(gl, gb);

//                java.awt.EventQueue.invokeLater(new Runnable() {
//
//                    public void run() {
//                        lsplash.setVisible(true);
//                        lsplash.setText("Synching with database " + name);
//                        lsplash.repaint();
//                    }
//                });


                    this.getRefiner().findHarmonics(cl.getCands(), period, name, null, candClass);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//                lsplash.setVisible(false);
//                lsplash.dispose();
//            }
//        });

    }

    public void writeExtra(PrintStream out) throws IOException {

        out.println("<DBUrl>" + this.databaseConnection.getUrl() + "</DBUrl>");
        out.println("<DBUsername>" + this.databaseConnection.getUsername() + "</DBUsername>");
        out.println("<DBPassword>" + this.databaseConnection.getPassword() + "</DBPassword>");
        out.println("<DBWebPath>" + this.internalWebRootPath + "</DBWebPath>");
        out.println("<DBWebURL>" + this.externalWebRootUrl + "</DBWebURL>");
        newDetection =
                true;
        if (newDetection) {
            this.findRedetections();
        }

    }
//    public void read(BufferedReader in) throws IOException {
//        super.read(in);
//        String[] elems;
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("URL")){
//            throw new IOException("Malformed DatabaseDataLibrary");
//        }
//
//        String url = elems[1];
//
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("USERNAME")){
//
//        }
//
//        String username = elems[1];
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("PASSWORD")){
//
//        }
//
//        String password = elems[1];
//
//        this.databaseConnection = new SQLDatabaseConnectionInfo(url,username,password);
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("WEBPATH")){
//
//        }
//        this.internalWebRootPath = elems[1];
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("WEBURL")){
//
//        }
//
//        this.externalWebRootUrl = elems[1];
//
//
//    }
    private double PERIOD_MATCH_FACTOR = 0.001;
    private double DM_MATCH_FACTOR = 0.2;
    private double DIST_FACTOR = 5;

    public void findRedetections() {
        System.out.println("Finding Matches in Database...");
        try {
            Statement stmt;
            Connection con;

            con =
                    DriverManager.getConnection(databaseConnection.getUrl(), databaseConnection.getUsername(), databaseConnection.getPassword());
            stmt =
                    con.createStatement();
            ResultSet rs;

            rs =
                    stmt.executeQuery("SELECT ID,Name,SNR_SPEC,SNR_RECON,SNR_FOLD,Status FROM `candidates` WHERE 1 ORDER BY Period,Dm,SNR_FOLD DESC");

//            long target_id = -1;
//            String target_name = ";lskd hjsd@@>:KLLJfa kl;jahsd"; // hopefully this will never match a real name!
//            double target_period = -100;
//            double target_snr    = -100;
//            double target_dm     = -100;
//            double target_gl     = -10000;
//            double target_gb     = -10000;

//            Hashtable<Long,ArrayList<Long>> idmap = new Hashtable<Long,ArrayList<Long>>();
//            Hashtable<Long,ArrayList<String>> statusmap = new Hashtable<Long,ArrayList<String>>();

            Hashtable<String, ArrayList<DBCand>> idmap = new Hashtable<String, ArrayList<DBCand>>();
//            Hashtable<String,ArrayList<String>> statusmap = new Hashtable<String,ArrayList<String>>();

            ArrayList<String> keys = new ArrayList<String>(); // the keys that have matches

            while (rs.next()) {
                // Match on names!



                long id = rs.getInt("ID");
//                double period = rs.getDouble("Period");
//                double dm = rs.getDouble("Dm");
//                double gl = rs.getDouble("Gl");
//                double gb = rs.getDouble("Gb");
                String name = rs.getString("Name");
                double snr1 = rs.getDouble("SNR_SPEC");
                double snr2 = rs.getDouble("SNR_RECON");
                double snr3 = rs.getDouble("SNR_FOLD");
                double snr = Math.max(Math.max(snr1, snr2), snr3);
                String status = rs.getString("Status");



                ArrayList<DBCand> idlist = idmap.get(name);
//                ArrayList<String> statusList = statusmap.get(name);
                if (idlist == null) {
                    idlist = new ArrayList<DBCand>();
                    idmap.put(name, idlist);

//                    statusList = new ArrayList<String>();
//                    statusmap.put(name,statusList);
                } else {
                    keys.add(name);
                }

                DBCand dbcand = new DBCand();
                dbcand.id = id;
                dbcand.status = status;
                dbcand.snr = snr;
                idlist.add(dbcand);
//                statusList.add(status);




            }


            /*
             * Now we have matched, update the database..
             *
             */
            for (String key : keys) {
                ArrayList<DBCand> idlist = idmap.get(key);
//                ArrayList<String> statusList = statusmap.get(key);

                String status = "NEW";
                Collections.sort(idlist, new DBCandComparator());
                for (int i = 0; i <
                        idlist.size(); i++) {
                    if (!(idlist.get(i).status.equals("NEW") || idlist.get(i).status.equals("ALIAS"))) {
                        status = idlist.get(i).status;
                        break;

                    }


                }
                long masterID = idlist.get(0).id;
                stmt.executeUpdate("UPDATE `candidates` SET `redetection`='0', `numdetections`='" + (idlist.size() - 1) + "', `Status`='" + status + "' Where `ID`='" + masterID + "';");

                for (int i = 1; i <
                        idlist.size(); i++) {
                    long id = idlist.get(i).id;
                    stmt.executeUpdate("UPDATE `candidates` SET `redetection`='" + masterID + "', `numdetections`='0', `Status`='ALIAS' WHERE `ID`='" + id + "';");
                }

            }




        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        System.out.println("Done Finding Matches in Database.");

    }

    /*
     *
     *  PVO database code for comparason
     *
     *   Statement stmt;
     *       Connection con;
     *       con = DriverManager.getConnection(info.getUrl(),info.getUsername(), info.getPassword());
     *       stmt = con.createStatement();
     *
     *       ResultSet rs =stmt.executeQuery("SELECT * FROM TaskTypes");
     *
     *       while(rs.next()){
     *           TaskTypeDescriptor tt = this.taskTypes.get(rs.getString("Type"));
     *           if(tt != null) result.add(tt);
     *       }
     *       con.close();
     *
     *
     */
    private class SQLpair {

        private  String key,  value;

        public SQLpair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getAsUpdate() {

            return "`" + key + "`='" + value + "'";
        }

        public String getAsInsertKey() {
            return key;
        }

        public String getAsInsertValue() {
            return value;
        }
    }
    

    static {

        try {
            //Register the JDBC driver for MySQL.
            Class.forName("com.mysql.jdbc.Driver");

        } catch (Exception e) {
            e.printStackTrace();
        }//end catch









    }

    public enum DatabaseDataLibraryXMLTypes {

        DBUrl, DBUsername, DBPassword, DBWebPath, DBWebURL, Other
    }
    private StringBuffer content = new StringBuffer();

    public void endElement(String uri, String localName, String qName) throws SAXException {
        DatabaseDataLibraryXMLTypes type = null;
        try {
            type = DatabaseDataLibraryXMLTypes.valueOf(localName);
        } catch (IllegalArgumentException e) {
            type = DatabaseDataLibraryXMLTypes.Other;
        }

        switch (type) {
            case DBUrl:
                this.databaseConnection.setUrl(content.toString().trim());
                break;

            case DBUsername:
                this.databaseConnection.setUsername(content.toString().trim());
                break;

            case DBPassword:
                this.databaseConnection.setPassword(content.toString().trim());
                break;

            case DBWebPath:
                this.internalWebRootPath = content.toString().trim();
                break;

            case DBWebURL:
                this.externalWebRootUrl = content.toString().trim();
                break;

            default:

                break;
        }

        content = new StringBuffer();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        content = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.content.append(ch, start, length);
    }

    private class DBCand {

        public double snr;
        public String status;
        public long id;
    }

    private class DBCandComparator implements Comparator<DBCand> {

        public int compare(DatabaseDataLibrary.DBCand o1, DatabaseDataLibrary.DBCand o2) {
            return (int) (100000.0 * (o2.snr - o1.snr));
        }
    }
}

