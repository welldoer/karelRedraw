package freditor.vector;

final class IntVector6 extends IntVectorN {
    private final int[][][][][][] root;
    private final int[] tail;

    IntVector6(int[][][][][] full, int x) {
        super(CAPACITY_5 + 1);

        root = new int[32][][][][][];
        root[0] = full;
        root[1] = new int[32][][][][];
        root[1][0] = new int[32][][][];
        root[1][0][0] = new int[32][][];
        root[1][0][0][0] = new int[32][];
        root[1][0][0][0][0] = tail = makeTail(x);
    }

    private IntVector6(int length, int[][][][][][] root, int[] tail) {
        super(length);
        this.root = root;
        this.tail = tail;
    }

    IntVector6(int length, int[][][][][][] root) {
        super(length);
        this.root = root;
        this.tail = leaf(length - 1);
    }

    @Override
    protected int[] leaf(int i) {
        return root[i >>> 25][(i >>> 20) & 31][(i >>> 15) & 31][(i >>> 10) & 31][(i >>> 5) & 31];
    }

    @Override
    public int intAt(int index) {
        return leaf(index)[index & 31];
    }

    @Override
    public int top() {
        return tail[(length - 1) & 31];
    }

    @Override
    public IntVector push(int x) {
        int a = length & 31;
        if (tail[a] == 0) return new IntVector6(length + 1, root, storeInto(tail, a, x));
        if (length == CAPACITY_6) throw new AssertionError("vector exhausted");

        int b = (length >>> 5) & 31;
        int c = (length >>> 10) & 31;
        int d = (length >>> 15) & 31;
        int e = (length >>> 20) & 31;
        int f = length >>> 25;

        int[][][][][][] F = root;
        int[][][][][] E = lazy(F[f]);
        int[][][][] D = lazy(E[e]);
        int[][][] C = lazy(D[d]);
        int[][] B = lazy(C[c]);
        int[] A = lazy(B[b]);

        A = with(A, a, x);
        B = with(B, b, A);
        C = with(C, c, B);
        D = with(D, d, C);
        E = with(E, e, D);
        F = with(F, f, E);

        return new IntVector6(length + 1, F);
    }

    @Override
    public IntVector pop() {
        int len1 = length - 1;
        if (len1 == CAPACITY_5) return new IntVector5(len1, root[0]);
        return new IntVector6(len1, root);
    }

    @Override
    public IntVector take(int n) {
        if (n == length) return this;
        if (n == CAPACITY_0) return IntVector.empty;
        if (n <= CAPACITY_1) return new IntVector1(n, root[0][0][0][0][0]);
        if (n <= CAPACITY_2) return new IntVector2(n, root[0][0][0][0]);
        if (n <= CAPACITY_3) return new IntVector3(n, root[0][0][0]);
        if (n <= CAPACITY_4) return new IntVector4(n, root[0][0]);
        if (n <= CAPACITY_5) return new IntVector5(n, root[0]);
        return new IntVector6(n, root);
    }
}