
package com.stratelia.webactiv.util.indexEngine.parser;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A piped parser is a Parser which uses a pipe to output the result
 * of his work.
 */
public abstract class PipedParser implements Parser
{

    /**
     * Write the text content of the file on the given output writer.
     */
    abstract protected void outPutContent(Writer out, String path, String encoding) throws IOException;

    /**
     * Returns a piped Reader giving the text content
     * extracted by the outPutContent method.
     */
    public Reader getReader(String path, String encoding)
    {
        PipedReader pipeIn = null;
        PipedWriter pipeOut = null;

        try
        {
            pipeIn = new PipedReader();
            pipeOut = new PipedWriter(pipeIn);

            new ParserThread(pipeOut, path, encoding).start();
        }
        catch (IOException e)
        {
            SilverTrace.error("indexEngine", "PipedParser", "indexEngine.MSG_PIPE_CREATION_FAILED", path, e);
            try
            {
                if (pipeIn != null)
                {
                    pipeIn.close();
                }
                if (pipeOut != null)
                {
                    pipeOut.close();
                }
            }
            catch (IOException ignored) {}

            pipeIn = null;
        }

        return pipeIn;
    }

    /**
     * Inner class which will run in the background
     * the outPutContent method.
     * 
     * Running this thread will provide
     * the character stream of the piped reader returned by the
     * getReader method and which is used by the main calling thread.
     */
    private class ParserThread extends Thread
    {

        /**
         * Builds a new thread running the command given by the path param.
         * 
         * 
         * @param out
         * @param path
         * @param encoding
         * 
         * @see
         */
        public ParserThread(Writer out, String path, String encoding)  // ????
        {
            this.out = out;
            this.path = path;
            this.encoding = encoding;
        }

        /**
         * Method declaration
         * 
         * 
         * @see
         */
        public void run()
        {
            if (out != null)
            {
                try
                {
                    outPutContent(out, path, encoding);
                }
                catch (IOException e)
                {

                    // Most IOExceptions are quasi normal here :
                    // when an indexed document is too huge,
                    // the lucene engine close the pipe as soon as
                    // he gets enought words : then the writer receives
                    // an IOException("Pipe closed").
                    SilverTrace.info("indexEngine", "PipedParser", "indexEngine.MSG_IO_ERROR_WHILE_PARSING", path, e);
                }
                finally
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e) {}
                }
            }
        }

        private final Writer out;
        private final String path;
        private final String encoding;
    }
}
