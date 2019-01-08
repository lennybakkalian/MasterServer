package de.fettesteil.masterserver;

public class TickThread implements Runnable {

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			synchronized (this) {
				try {

					for (int i = Main.clientList.size() - 1; i >= 0; i--) {
						Client c = Main.clientList.get(i);
						if (c != null && !c.isAuthenticated())
							if (System.currentTimeMillis() - c.getConnectedSince() > Main.LOGIN_TIMEOUT)
								c.disconnect();
					}

					Thread.currentThread().sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
