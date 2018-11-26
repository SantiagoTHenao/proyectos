package bbva.prueba.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class gitManagement {

	public static void cloneRepoHTTP(String folder,String URI) {
		try {
			Git.cloneRepository().setURI(URI).setDirectory(new File(folder)).setCloneAllBranches(true).call();
		} catch (InvalidRemoteException e) {
			System.err.println("Error: " + e.getMessage());
		} catch (TransportException e) {
			System.err.println("Error: " + e.getMessage());
		} catch (GitAPIException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void cloneRepoSSH(String rutaRepoRemoto, String rutaRepo)
			throws InvalidRemoteException, TransportException, GitAPIException {

		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(rutaRepoRemoto);
		cloneCommand.setDirectory(new File(rutaRepo));
		cloneCommand.setTransportConfigCallback(getTransportConfigCallback());
		cloneCommand.call();

	}

	public static TransportConfigCallback getTransportConfigCallback() {

		final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure(Host arg0, Session arg1) {
				// NULL
			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {
				JSch defaultJSch = super.createDefaultJSch(fs);
				defaultJSch.addIdentity("C:/Users/User/Documents/MobaXterm/home/.ssh/id_rsa");
				return defaultJSch;
			}

		};
		return new TransportConfigCallback() {
			@Override
			public void configure(Transport transport) {
				SshTransport sshTransport = (SshTransport) transport;
				sshTransport.setSshSessionFactory(sshSessionFactory);
			}
		};

	}

	public static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {

		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}

}
