package bbva.prueba;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import bbva.prueba.git.gitManagement;
import bbva.prueba.zip.zipManagement;

public class Main {

	public static void main(String[] args) throws IOException, RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException {

		Date myDate = new Date();
		final String fecha = new SimpleDateFormat("yyyyMMddHHmmss").format(myDate);

		final String branchChanges = "GP16014_Banca_MovilFXv2";
		final String rutaRepo = "C:\\Users\\User\\Desktop\\MensajeriaQA";
		final String rutaRepoRemoto = "ssh://git@globaldevtools.bbva.com:7999/bcmo/mensajeria_online_qa.git";
		final String rutaVersion = "C:\\Users\\User\\Desktop\\versiones\\"+fecha+"\\"+branchChanges+"\\";
		final String rutaVersionZIP = "C:\\Users\\User\\Desktop\\versiones\\"+fecha+"\\"+branchChanges;

		/* Clonando repositorio */
		new File(rutaRepo);
		gitManagement.cloneRepoSSH(rutaRepoRemoto, rutaRepo);
		File gitWorkDir = new File(rutaRepo);
		Git git = Git.open(gitWorkDir);

		/* Obtener commit de master y rama a comparar */
		Ref master = git.getRepository().exactRef("HEAD");

		git.checkout().setCreateBranch(true).setName(branchChanges).setUpstreamMode(SetupUpstreamMode.TRACK)
				.setStartPoint("origin/" + branchChanges).call();

		Ref branch = git.getRepository().exactRef("HEAD");

		/* Hallar diferencias entre commits */

		AbstractTreeIterator newTreeParser = gitManagement.prepareTreeParser(git.getRepository(),
				branch.getObjectId().getName());
		AbstractTreeIterator oldTreeParser = gitManagement.prepareTreeParser(git.getRepository(),
				master.getObjectId().getName());

		List<DiffEntry> diffs = git.diff().setNewTree(newTreeParser).setOldTree(oldTreeParser).call();

		/* Mover Archivos con modificaciones a distinta carpeta */
		if (diffs.size() > 0) {
			int c = 0;
			while (c < diffs.size()) {

				new File(rutaVersion + diffs.get(c).getNewPath()).mkdirs();
				zipManagement.moverArchivo(git.getRepository().getWorkTree() + "/" + diffs.get(c).getNewPath(),
						rutaVersion + diffs.get(c).getNewPath());
				c++;
			}

			File dir = new File(rutaVersion);
			String zipDirName = rutaVersionZIP + ".zip";

			zipManagement.zipDirectory(dir, zipDirName);

		} else {
			System.out.println("No existen diferencia entre Master y la rama ");
		}

	}

}
