
package io.github.zeroaicy.aide.scm;

import org.eclipse.jgit.api.*;

import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;
import com.aide.common.AppLog;
import com.aide.common.StreamUtilities;
import com.aide.ui.scm.ExternalGitService;
import com.aide.ui.scm.GitConfiguration;
import com.aide.ui.scm.GitStatus;
import com.aide.ui.scm.IExternalGitService;
import com.aide.ui.scm.IExternalGitServiceListener;
import com.aide.ui.scm.ModifiedFile;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

public class ZeroAicyExternalGitService extends ExternalGitService {

	private GitServiceImpl gitServiceImpl;
	public ZeroAicyExternalGitService() {
		this.gitServiceImpl = new GitServiceImpl(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		AppLog.i("ZeroAicyExternalGitService bound - pid " + Process.myPid() + " id " + System.identityHashCode(this));
		
		FileSystem.init(this);

		return this.gitServiceImpl;
	}

	@Override
	public void onDestroy() {
		AppLog.i("ZeroAicyExternalGitService shutdown - pid " + Process.myPid() + " id "
				 + System.identityHashCode(this));
		this.gitServiceImpl = null;

	}

	private class UserConfigSystemReader extends DefaultSystemReader {

		public UserConfigSystemReader(GitServiceImpl gitServiceImpl, SystemReader systemReader,
									  ZeroAicyExternalGitService ZeroAicyExternalGitService) {
			super(systemReader);
		}

		@Override
		public FileBasedConfig openJGitConfig(Config config, FS fs) {
			return openUserConfig(config, fs);
		}

		@Override
		public FileBasedConfig openUserConfig(Config config, FS fs) {
			return new FileBasedConfig(config, new File(FileSystem.getCacheDir(), ".gitconfig"), fs);
		}
	}

	private static class DefaultSystemReader extends SystemReader {
		private SystemReader defaultSystemReader;

		public DefaultSystemReader(SystemReader systemReader) {
			this.defaultSystemReader = systemReader;
		}

		public long getCurrentTime() {
			return this.defaultSystemReader.getCurrentTime();
		}

		public String getHostname() {
			return this.defaultSystemReader.getHostname();
		}

		public String getProperty(String str) {
			return this.defaultSystemReader.getProperty(str);
		}

		public int getTimezone(long j) {
			return this.defaultSystemReader.getTimezone(j);
		}

		public String getenv(String str) {
			return this.defaultSystemReader.getenv(str);

		}

		public FileBasedConfig openSystemConfig(Config config, FS fs) {
			return this.defaultSystemReader.openSystemConfig(config, fs);

		}

		public FileBasedConfig openUserConfig(Config config, FS fs) {
			return this.defaultSystemReader.openUserConfig(config, fs);
		}

		@Override
		public FileBasedConfig openJGitConfig(Config config, FS fs) {
			return this.defaultSystemReader.openJGitConfig(config, fs);
		}
	}

	/* loaded from: /storage/emulated/0/AppProjects1/.ZeroAicy/git/AIDE+/build/jadx/gitImpl.dex */
	private class GitServiceImpl extends IExternalGitService.Sub {

		public static final String TAG = "GitServiceImpl";

		public class AideCredentialsProvider extends CredentialsProvider {
			final IExternalGitServiceListener externalGitServiceListener;

			AideCredentialsProvider(GitServiceImpl gitServiceImpl,
									IExternalGitServiceListener externalGitServiceListener) {
				this.externalGitServiceListener = externalGitServiceListener;
			}

			private boolean get(URIish uRIish, CredentialItem credentialItem) {
				if (credentialItem instanceof CredentialItem.StringType) {
					try {
						String jB = this.externalGitServiceListener.jB(credentialItem.getPromptText());
						if (jB == null) {
							return false;
						}
						((CredentialItem.StringType) credentialItem).setValue(jB);
						return true;
					}
					catch (Exception e) {
						AppLog.e(e);
						return false;
					}
				}
				if (credentialItem instanceof CredentialItem.CharArrayType) {
					try {
						String jB2 = this.externalGitServiceListener.jB(credentialItem.getPromptText());
						if (jB2 == null) {
							return false;
						}
						((CredentialItem.CharArrayType) credentialItem).setValue(jB2.toCharArray());
						return true;
					}
					catch (Throwable e2) {
						AppLog.e(e2);
						return false;
					}
				}
				if (credentialItem instanceof CredentialItem.InformationalMessage) {
					try {
						this.externalGitServiceListener.iE(credentialItem.getPromptText());
						return true;
					}
					catch (Throwable e3) {
						AppLog.e(e3);
						return false;
					}
				}
				if (credentialItem instanceof CredentialItem.YesNoType) {
					try {
						String Lg = this.externalGitServiceListener.Lg(credentialItem.getPromptText());
						if (Lg == null) {
							return false;
						}
						((CredentialItem.YesNoType) credentialItem).setValue(Boolean.parseBoolean(Lg));
						return true;
					}
					catch (Throwable e4) {
						AppLog.e(e4);
						return false;
					}
				}
				throw new UnsupportedCredentialItemException(uRIish,
															 "Unsupported CredentialItem: " + credentialItem.getClass().getName());
			}

			private boolean support(CredentialItem credentialItem) {
				if (!(credentialItem instanceof CredentialItem.StringType)
					&& !(credentialItem instanceof CredentialItem.CharArrayType)
					&& !(credentialItem instanceof CredentialItem.InformationalMessage)) {
					if (!(credentialItem instanceof CredentialItem.YesNoType)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public boolean get(URIish uRIish, CredentialItem... credentialItemArr) {
				for (CredentialItem credentialItem : credentialItemArr) {
					if (!get(uRIish, credentialItem)) {
						return false;
					}
				}
				return true;
			}

			public boolean isInteractive() {
				return true;
			}

			@Override
			public boolean supports(CredentialItem... credentialItemArr) {
				for (CredentialItem credentialItem : credentialItemArr) {
					if (!support(credentialItem)) {
						return false;
					}
				}
				return true;
			}
		}

		private class AideProgressMonitor implements ProgressMonitor {

			private int DW;

			private IExternalGitServiceListener externalGitServiceListener;

			final GitServiceImpl gitServiceImpl;

			public AideProgressMonitor(GitServiceImpl gitServiceImpl,
									   IExternalGitServiceListener externalGitServiceListener) {
				this.gitServiceImpl = gitServiceImpl;
				this.externalGitServiceListener = externalGitServiceListener;
			}

			public void beginTask(String str, int i) {
				try {
					this.DW = 0;
					this.externalGitServiceListener.d4(str, i);
				}
				catch (Throwable e) {
					AppLog.e(e);
				}
			}

			public void endTask() {
				try {
					this.externalGitServiceListener.gy();
				}
				catch (Throwable e) {
					AppLog.e(e);
				}
			}

			public boolean isCancelled() {
				synchronized (this.gitServiceImpl.jw) {
					return this.gitServiceImpl.canceled;
				}
			}

			public void start(int i) {
				try {
					this.externalGitServiceListener.Yd(i);
				}
				catch (Throwable e) {
					AppLog.e(e);
				}
			}

			public void update(int i) {
				try {
					int i2 = this.DW + i;
					this.DW = i2;
					this.externalGitServiceListener.AJ(i2);
				}
				catch (Throwable e) {
					AppLog.e(e);
				}
			}

			@Override
			public void showDuration(boolean showDuration) {

			}

		}

		private String userEmail;

		private boolean canceled;

		private Object jw;

		private String k2;

		private Object qp;

		private String userName;

		public GitServiceImpl(ZeroAicyExternalGitService ZeroAicyExternalGitService) {
			this.jw = new Object();
			this.qp = new Object();
			this.k2 = FileSystem.getExternalStorageDirectory();
			FS.DETECTED.setUserHome(new File(this.k2));
			SystemReader.setInstance(
				new UserConfigSystemReader(this, SystemReader.getInstance(), ZeroAicyExternalGitService));
		}

		private void showErrorMessage(IExternalGitServiceListener externalGitServiceListener, String gitCommandName,
									  Throwable commandError) {

			if (externalGitServiceListener == null) {
				return;
			}

			if (commandError instanceof OutOfMemoryError) {
				try {
					externalGitServiceListener.J1();
				}
				catch (Throwable e) {
					AppLog.e(e);
				}
				AppLog.d("Git service process killed after OOM");
				Process.killProcess(Process.myPid());
				return;
			}

			String errorMessage = getErrorMessage(commandError);
			if (errorMessage == null) {
				errorMessage = "";
			}
			// set error 异步
			externalGitServiceListener.qi(gitCommandName + " failed: " + errorMessage);
			
			// 等待set error，show也是异步
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {}
		}

		private String BR(String str, String str2) {
			if (!str.endsWith(File.separator)) {
				str = str + File.separator;
			}
			if (str2.startsWith(str)) {
				return str2.substring(str.length());
			}
			throw new IllegalArgumentException(str2 + " not in repository " + str);

		}

		private void Bx(IExternalGitServiceListener externalGitServiceListener) {
			synchronized (this.jw) {
				if (this.canceled) {
					this.canceled = false;
					externalGitServiceListener.oP();
				}
			}
		}

		private void CU(String str, List<ModifiedFile> list, Iterable<String> iterable, int i, Set<String> set,
						boolean z) {
			for (String str2 : iterable) {
				if (set == null || !set.contains(str2)) {
					list.add(new ModifiedFile(new File(str, str2).getPath(), i));
				}
			}
		}

		private void Ev(String gitDir, List<ModifiedFile> modifiedFiles, String str2,
						IExternalGitServiceListener externalGitServiceListener) throws Exception {
			Git open = Git.open(new File(gitDir));
			try {
				AddCommand add = open.add();
				boolean z = false;
				for (ModifiedFile modifiedFile : modifiedFiles) {
					if (modifiedFile.v5() || modifiedFile.gn() || modifiedFile.Zo() || modifiedFile.Hw()) {
						add.addFilepattern(BR(gitDir, modifiedFile.WB));
						z = true;
					}
				}
				if (z) {
					add.call();
				}
				Bx(externalGitServiceListener);
				CommitCommand commit = open.commit();
				commit.setAll(false);
				commit.setMessage(str2);
				Iterator<ModifiedFile> it = modifiedFiles.iterator();
				while (it.hasNext()) {
					commit.setOnly(BR(gitDir, it.next().WB));
				}
				commit.call();
			}
			finally {
				open.getRepository().close();
			}

		}

		private void Lz(Git git, String str, OutputStream outputStream) throws IOException {
			RevWalk revWalk = null;
			try {
				revWalk = new RevWalk(git.getRepository());
				RevTree tree = revWalk.parseCommit(git.getRepository().resolve("HEAD")).getTree();

				ObjectStream openStream = git.getRepository()
					.open(TreeWalk.forPath(git.getRepository(), str, tree).getObjectId(0), 3).openStream();
				try {
					StreamUtilities.transferStream(openStream, outputStream);
				}
				finally {
					IOUtils.close(openStream);
				}
			}
			finally {
				IOUtils.close(revWalk);
			}
		}

		private void settingGitConfiguration(GitConfiguration gitConfiguration) {
			synchronized (this.qp) {
				String sshFilePath = gitConfiguration.jw;
				if (sshFilePath != null && sshFilePath.endsWith(".ssh")) {

					String sshFileParentDir = sshFilePath.substring(0, sshFilePath.length() - 4);

					boolean isUpdate = sshFileParentDir.equals(this.k2);
					if (!isUpdate) {
						this.k2 = sshFileParentDir;
						FS.DETECTED.setUserHome(new File(this.k2));
						SshSessionFactory.setInstance((SshSessionFactory) null);
					}
				}

				String userName = gitConfiguration.WB;
				String userEmail = gitConfiguration.mb;

				if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userEmail)) {
					return;
				}
				// 是否需要更新
				boolean isUpdate = !userName.equals(this.userName) || !userEmail.equals(this.userEmail);

				if (isUpdate) {
					FileWriter fileWriter = null;
					PrintWriter printWriter = null;
					try {
						File gitconfigFile = new File(FileSystem.getCacheDir(), ".gitconfig");
						// 删除文件
						gitconfigFile.delete();

						fileWriter = new FileWriter(gitconfigFile);
						printWriter = new PrintWriter(fileWriter);
						printWriter.println("[user]");
						printWriter.println("\tname = " + userName.trim());
						printWriter.println("\temail = " + userEmail.trim());

						this.userName = userName;
						this.userEmail = gitConfiguration.mb;
					}
					catch (Throwable e) {
						AppLog.e(e);
					}
					finally {
						IOUtils.close(printWriter);
						IOUtils.close(fileWriter);
					}
				}
			}
		}

		private void addOriginRemote(String gitDir, String originRemoteUrl,
									 IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					StoredConfig config = open.getRepository().getConfig();
					config.setString("remote", "origin", "url", originRemoteUrl);
					config.save();
				}
				finally {
					IOUtils.close(open.getRepository());
				}
			}
			catch (Throwable th2) {
				showErrorMessage(externalGitServiceListener, "Git push", th2);
			}
		}

		private String getErrorMessage(Throwable commandError) {
			return getErrorMessage(commandError, 0, 100);

		}

		private GitStatus pN(String str, Git git, Status status, Map<String, SubmoduleStatus> map) {
			Set<String> keySet = map.keySet();
			ArrayList<ModifiedFile> arrayList = new ArrayList<>();
			CU(str, arrayList, status.getAdded(), 1, null, false);
			CU(str, arrayList, status.getChanged(), 2, null, true);
			CU(str, arrayList, status.getConflicting(), 64, null, true);
			CU(str, arrayList, status.getMissing(), 8, keySet, true);
			CU(str, arrayList, status.getModified(), 16, null, true);
			CU(str, arrayList, status.getRemoved(), 4, null, true);
			CU(str, arrayList, status.getUntracked(), 32, null, false);
			return new GitStatus(str, arrayList);

		}

		private CredentialsProvider getCredentialsProvider(IExternalGitServiceListener externalGitServiceListener) {
			return new AideCredentialsProvider(this, externalGitServiceListener);
		}

		private String getErrorMessage(Throwable commandError, int currentDepth, int depth) {

			String commandErrorMessage;

			if (currentDepth >= depth || commandError.getCause() == null
				|| (commandErrorMessage = getErrorMessage(commandError.getCause(), currentDepth + 1,
														  depth)) == null) {
				return commandError.getMessage();
			} else {
				return commandErrorMessage;
			}
		}

		@Override
		public void Bl(GitConfiguration gitConfiguration, String str, List<ModifiedFile> list, String str2,
					   IExternalGitServiceListener externalGitServiceListener) {
			try {
				settingGitConfiguration(gitConfiguration);
				Ev(str, list, str2, externalGitServiceListener);
				Bx(externalGitServiceListener);
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git commit", th);
			}

		}


		// git push
		@Override
		public void Cd(GitConfiguration gitConfiguration, String gitDir,
					   IExternalGitServiceListener externalGitServiceListener) {
			try {

				settingGitConfiguration(gitConfiguration);

				Git git = Git.open(new File(gitDir));
				try {
					PushCommand pushCommand = git.push();
					pushCommand.setCredentialsProvider(getCredentialsProvider(externalGitServiceListener));
					pushCommand.setProgressMonitor(new AideProgressMonitor(this, externalGitServiceListener));
					
					boolean everythingUpToDate = true;
					for (PushResult pushResult : pushCommand.call()) {
						for (RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates()) {
							if( remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.UP_TO_DATE ){
								continue;
							}
							// 非UP_TO_DATE 有文件更新
							everythingUpToDate = false;
							
							if (remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.OK) {
								continue;
							}
							
							// push错误
							throw new Exception("Messages: " + pushResult.getMessages() + " Status: "
												+ remoteRefUpdate.getStatus());
						}
					}
					
					// 没有更新
					if( everythingUpToDate ){
						throw new Exception("Messages: Everything up-to-date [一切都是最新的] ");
					}

					// 保留SSH会话信息
					if (!gitConfiguration.fY) {
						SshSessionFactory.setInstance(null);
					}
					git.getRepository().close();

					Bx(externalGitServiceListener);
				}
				catch (Throwable th) {
					if (!gitConfiguration.fY) {
						SshSessionFactory.setInstance(null);
					}
					git.getRepository().close();
					throw th;
				}
			}
			catch (Throwable th) {
				if (th.getCause() instanceof NoRemoteRepositoryException) {
					// 请指定有效的远程存储库url:
					String originRemoteUrl = externalGitServiceListener.nV("Please specifiy a valid remote repository url:");
					if (originRemoteUrl != null) {
						addOriginRemote(gitDir, originRemoteUrl, externalGitServiceListener);
						Cd(gitConfiguration, gitDir, externalGitServiceListener);
						return;
					}
					return;
				}

				showErrorMessage(externalGitServiceListener, "Git push", th);
			}

		}
		
		// git merge
		@Override
		public void HE(String gitDir, String str2, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					
					MergeCommand merge = open.merge();
					merge.include(open.getRepository().resolve(str2));
					MergeResult call = merge.call();
					if (call.getMergeStatus() != MergeResult.MergeStatus.FAILED) {
						return;
					}
					throw new Exception("Git merge failed: " + call.getMergeStatus().toString());
				}
				finally {
					open.getRepository().close();
				}
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git merge", th);
			}

		}
		
		// git clone
		@Override
		public void IE(GitConfiguration gitConfiguration, String str, String str2, String str3,
					   IExternalGitServiceListener externalGitServiceListener) {
			try {
				
				settingGitConfiguration(gitConfiguration);
				CloneCommand cloneRepository = Git.cloneRepository();
				cloneRepository.setDirectory(new File(str, str2));
				cloneRepository.setCloneAllBranches(true);
				cloneRepository.setCredentialsProvider(getCredentialsProvider(externalGitServiceListener));
				CloneCommand cloneCommand = cloneRepository;
				cloneCommand.setProgressMonitor(new AideProgressMonitor(this, externalGitServiceListener));
				cloneCommand.setURI(str3);
				cloneCommand.call().getRepository().close();
				Bx(externalGitServiceListener);
			}
			catch (Throwable th) {
				if (!gitConfiguration.fY) {
					SshSessionFactory.setInstance((SshSessionFactory) null);
				}
				showErrorMessage(externalGitServiceListener, "Git clone", th);
			}

		}

		@Override
		public String JT(String gitDir, String str2, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				FileOutputStream fileOutputStream = null;
				try {
					File createTempFile = File.createTempFile("gitcontent", ".txt",
															  new File(FileSystem.getSafeCacheDirPath()));
					fileOutputStream = new FileOutputStream(createTempFile);
					Lz(open, BR(gitDir, str2), fileOutputStream);
					Bx(externalGitServiceListener);
					return createTempFile.getPath();
				}
				finally {
					open.getRepository().close();
					IOUtils.close(fileOutputStream);
				}
			}
			catch (Throwable th2) {
				showErrorMessage(externalGitServiceListener, "Git get base file content", th2);
			}
			return null;
		}
		
		// git delete branch
		@Override
		public void MN(String gitDir, String str2, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					DeleteBranchCommand branchDelete = open.branchDelete();
					branchDelete.setBranchNames(new String[]{str2});
					branchDelete.call();
					open.getRepository().close();
				}
				catch (Throwable th) {
					open.getRepository().close();

				}
			}
			catch (Throwable th2) {
				showErrorMessage(externalGitServiceListener, "Git delete branch", th2);
			}

		}

		@Override
		public List<String> cS(String gitDir, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					ListBranchCommand branchList = open.branchList();
					branchList.setListMode(ListBranchCommand.ListMode.ALL);
					List<Ref> call = branchList.call();
					ArrayList<String> arrayList = new ArrayList<>();
					Iterator<Ref> it = call.iterator();
					while (it.hasNext()) {
						arrayList.add(it.next().getName());
					}
					return arrayList;
				}
				finally {
					open.getRepository().close();
				}
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git get branches", th);
				return null;
			}

		}

		@Override
		public void cancel() {
			synchronized (this.jw) {
				this.canceled = true;
			}

		}

		@Override
		public void cz(String gitDir, String str2, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					CheckoutCommand checkout = open.checkout();
					checkout.setName(str2);
					checkout.call();
					if (checkout.getResult().getStatus() == CheckoutResult.Status.OK) {
						return;
					}
					throw new Exception("Git checkout failed: " + checkout.getResult().getStatus());
				}
				finally {
					open.getRepository().close();
				}
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git checkout", th);
			}

		}

		@Override
		public GitStatus el(String gitDir, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					Status call = open.status().call();
					Map<String, SubmoduleStatus> call2 = open.submoduleStatus().call();
					Bx(externalGitServiceListener);
					return pN(gitDir, open, call, call2);
				}
				finally {
					open.getRepository().close();
				}
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git status", th);
				return null;
			}

		}

		@Override
		public void of(GitConfiguration gitConfiguration, String str, String str2, List<String> list,
					   IExternalGitServiceListener externalGitServiceListener) {
			try {
				settingGitConfiguration(gitConfiguration);

				new File(str).mkdirs();

				FileRepository fileRepository = new FileRepository(new File(str, ".git"));
				fileRepository.create();
				fileRepository.close();
				FileWriter fileWriter = new FileWriter(new File(str, ".gitignore"));
				Iterator<String> it = list.iterator();
				while (it.hasNext()) {
					fileWriter.write(it.next() + "\n");
				}
				fileWriter.close();
				GitStatus el = el(str, externalGitServiceListener);
				if (el.mb.size() > 0) {
					Ev(str, el.mb, str2, externalGitServiceListener);
				}
				Bx(externalGitServiceListener);
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git create", th);
			}

		}

		@Override
		public void wN(GitConfiguration gitConfiguration, String gitDir,
					   IExternalGitServiceListener externalGitServiceListener) {
			try {
				settingGitConfiguration(gitConfiguration);
				Git open = Git.open(new File(gitDir));
				try {
					PullCommand pull = open.pull();
					pull.setCredentialsProvider(getCredentialsProvider(externalGitServiceListener));
					PullCommand pullCommand = pull;
					pullCommand.setProgressMonitor(new AideProgressMonitor(this, externalGitServiceListener));
					PullResult call = pullCommand.call();
					if (!call.isSuccessful()) {
						if (call.getMergeResult() != null
							&& call.getMergeResult().getMergeStatus() == MergeResult.MergeStatus.CONFLICTING) {
							throw new Exception(call.getMergeResult().toString());
						}
						throw new Exception(call.toString());
					}
					if (!gitConfiguration.fY) {
						SshSessionFactory.setInstance((SshSessionFactory) null);
					}
					open.getRepository().close();
					Bx(externalGitServiceListener);
				}
				catch (Throwable th) {
					if (!gitConfiguration.fY) {
						SshSessionFactory.setInstance((SshSessionFactory) null);
					}
					open.getRepository().close();

				}
			}
			catch (Exception unused) {
				Bx(externalGitServiceListener);
			}
			catch (Throwable th2) {
				showErrorMessage(externalGitServiceListener, "Git pull", th2);
			}

		}

		@Override
		public void wX(String gitDir, String str2, String str3, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					CreateBranchCommand branchCreate = open.branchCreate();
					branchCreate.setName(str2);
					if (str3 != null) {
						branchCreate.setStartPoint(str3);
						branchCreate.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
					}
					branchCreate.call();
					CheckoutCommand checkout = open.checkout();
					checkout.setName(str2);
					checkout.call();
					if (checkout.getResult().getStatus() == CheckoutResult.Status.OK) {
						return;
					}
					throw new Exception("Git checkout failed: " + checkout.getResult().getStatus());
				}
				finally {
					open.getRepository().close();
				}
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git checkout", th);
			}

		}

		@Override
		public void yj(String gitDir, List<ModifiedFile> list, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					CheckoutCommand checkout = open.checkout();
					checkout.setForced(true);
					checkout.setStartPoint("HEAD");
					Iterator<ModifiedFile> it = list.iterator();
					while (it.hasNext()) {
						checkout.addPath(BR(gitDir, it.next().WB));
					}
					checkout.call();
					if (checkout.getResult().getStatus() == CheckoutResult.Status.OK) {
						for (ModifiedFile modifiedFile : list) {
							if (modifiedFile.DW() || modifiedFile.gn()) {
								File file = new File(modifiedFile.WB);
								if (file.exists() && !file.delete()) {
									throw new IOException("Could not delete " + modifiedFile.WB);
								}
								open.getRepository().close();
								Bx(externalGitServiceListener);
								return;
							}
						}
						open.getRepository().close();
						Bx(externalGitServiceListener);
						return;
					}
					throw new Exception("Git checkout failed: " + checkout.getResult().getStatus());
				}
				catch (Throwable th) {
					open.getRepository().close();

				}
			}
			catch (Throwable th2) {
				showErrorMessage(externalGitServiceListener, "Git discard", th2);
			}

		}

		@Override
		public String zg(String gitDir, IExternalGitServiceListener externalGitServiceListener) {
			try {
				Git open = Git.open(new File(gitDir));
				try {
					return open.getRepository().getFullBranch();
				}
				finally {
					open.getRepository().close();
				}
			}
			catch (Throwable th) {
				showErrorMessage(externalGitServiceListener, "Git get branches", th);
				return null;
			}

		}
	}
}

