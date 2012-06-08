package edu.stanford.bmir.protege.web.server.phenotypes;
public class StringMatching {

	public String txt, string, matchedString;
	public float score;

	protected float align(String A, String B) { //A=Term B=TEXT
		this.string = A;
		this.txt = B;

		float dx = (float) 0.8;
		float dy = (float) 0.8;
		float ex = (float) 0.25;
		float ey = (float) 0.25;
		float[][] M = new float[B.length() + 1][A.length() + 1];
		float[][] Ix = new float[B.length() + 1][A.length() + 1];
		float[][] Iy = new float[B.length() + 1][A.length() + 1];

		for (int j = 1; j < B.length() + 1; ++j) {
			for (int i = 1; i < A.length() + 1; ++i) {
				M[j][i] = max(M[j - 1][i - 1], Ix[j - 1][i - 1],
						Iy[j - 1][i - 1])
						+ score(A.charAt(i - 1), B.charAt(j - 1));
				Ix[j][i] = Math.max(M[j][i - 1] - dy, Ix[j][i - 1] - ey);
				Iy[j][i] = Math.max(M[j - 1][i] - dx, Iy[j - 1][i] - ex);

			}
		}

		int ib = 0, ia = 0;
		float maxScore = (float) 0.0;
		for (int i = 1; i < B.length() + 1; i++) {
			if (maxScore < M[i][A.length()]) {
				maxScore = M[i][A.length()];
				ib = i;
			}
		}
		for (int i = 1; i < A.length() + 1; i++) {
			if (maxScore < M[B.length()][i]) {
				maxScore = M[B.length()][i];
				ia = i;
			}
		}

		int spaceIndex = B.lastIndexOf(' ', ib - A.length()+1);
		if (ib >= A.length() && spaceIndex>0) {
			this.matchedString = B.substring(spaceIndex, ib);
		}else{
			this.matchedString = B.substring(0, ib);
		}
		// System.out.println("A = " + A + " B = " + B + " SCORE " + maxScore);
		this.score = (maxScore) / (A.length());
		return score;

	}

	protected float score(char a, char b) {
		if (a == b)
			return (float) 1.0;
		else
			return (float) -1.0;
	}

	protected float max(float a, float b, float c) {
		return Math.max(Math.max(a, b), Math.max(a, c));
	}
}
