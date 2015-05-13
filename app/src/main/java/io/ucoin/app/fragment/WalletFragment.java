package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.adapter.CertificationListAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.UnitType;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.WotCertification;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;


public class WalletFragment extends Fragment {

    public static final String TAG = "WalletFragment";

    private static String ARGS_TAB_INDEX = "tabIndex";

    private ProgressViewAdapter mProgressViewAdapter;
    private CertificationListAdapter mCertificationListAdapter;
    private TextView mUidView;
    private ImageButton mIcon;
    private TextView mTimestampLabelView;
    private TextView mTimestampView;
    private TextView mPubkeyView;
    private TextView mCreditView;
    private TextView mCurrencyView;
    private TabHost mTabs;

    private String mUnitType;

    private boolean mSignatureSingleLine = true;
    private boolean mPubKeySingleLine = true;

    public static WalletFragment newInstance(Wallet wallet) {
        WalletFragment fragment = new WalletFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Wallet.class.getSimpleName(), wallet);
        newInstanceArgs.putInt(ARGS_TAB_INDEX, 0);
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    public static WalletFragment newInstance(Wallet wallet, int tabIndex) {
        WalletFragment fragment = new WalletFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Wallet.class.getSimpleName(), wallet);
        newInstanceArgs.putInt(ARGS_TAB_INDEX, tabIndex);
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_wallet,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());
        final int tabIndex = newInstanceArgs.getInt(ARGS_TAB_INDEX);

        // Tab host
        mTabs = (TabHost)view.findViewById(R.id.tabHost);
        mTabs.setup();
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab1");
            spec.setContent(R.id.tab1);
            spec.setIndicator(getString(R.string.transactions));
            mTabs.addTab(spec);
        }
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab2");
            spec.setContent(R.id.tab2);
            spec.setIndicator(getString(R.string.community));
            mTabs.addTab(spec);
        }
        mTabs.setCurrentTab(tabIndex);

        //Uid
        mUidView = (TextView) view.findViewById(R.id.uid);

        // details view
        final View detailView = view.findViewById(R.id.details);
        detailView.setVisibility(View.GONE);

        // Toogle detail button
        final ImageButton toogleDetailButton = (ImageButton) view.findViewById(R.id.toogle_detail);
        toogleDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailView.getVisibility() == View.VISIBLE) {
                    detailView.setVisibility(View.GONE);
                    toogleDetailButton.setImageResource(R.drawable.expander_open_holo_dark);
                } else {
                    detailView.setVisibility(View.VISIBLE);
                    toogleDetailButton.setImageResource(R.drawable.expander_close_holo_dark);
                }
            }
        });

        // Icon
        mIcon = (ImageButton)view.findViewById(R.id.icon);

        // Timestamp label
        mTimestampLabelView = (TextView) view.findViewById(R.id.timestamp_label);

        // Timestamp
        mTimestampView = (TextView) view.findViewById(R.id.timestamp);

        // Pub key
        mPubkeyView = (TextView) view.findViewById(R.id.pubkey);

        // Currency
        mCurrencyView = (TextView) view.findViewById(R.id.currency);

        // Credit
        mCreditView = (TextView) view.findViewById(R.id.credit);

        // Tab 1: transfer list
        MovementListFragment tab1Fragment = MovementListFragment.newInstance(wallet);
        getFragmentManager().beginTransaction()
                .replace(R.id.tab1, tab1Fragment, "tab1")
                .commit();

        // Wot list
        ListView wotListView = (ListView) view.findViewById(R.id.wot_list);
        wotListView.setVisibility(View.GONE);
        mCertificationListAdapter = new CertificationListAdapter(getActivity());
        wotListView.setAdapter(mCertificationListAdapter);

        //this listener is not called unless WotExpandableListAdapter.isChildSelectable return true
        //and convertView.onClickListener is not set (in WotExpandableListAdapter)
        wotListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onWotIdentityClick(position);
            }
        });

        //PROGRESS VIEW
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.load_progress),
                wotListView);

        // Make sure to hide the keyboard
        ViewUtils.hideKeyboard(getActivity());

        // Read unit type from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);

        // update views
        updateView(wallet);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);

        MenuItem selfMenu = menu.findItem(R.id.action_self);
        MenuItem joinMenu = menu.findItem(R.id.action_join);

        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

        if (wallet.getIsMember() || wallet.getCertTimestamp() > 0) {
            selfMenu.setVisible(false);
        }
        if (wallet.getIsMember()) {
            joinMenu.setVisible(false);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        activity.setTitle(R.string.wallet_title);
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
            ((IToolbarActivity) activity).setToolbarDrawable(getResources().getDrawable(R.drawable.shape_wallet_toolbar));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_transfer:
                onTransferClick();
                return true;
            case R.id.action_self:
                onSelfClick();
                return true;
            case R.id.action_join:
                onRequestMembershipClick();
                return true;
            case R.id.action_delete:
                onDeleteClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* -- protected methods -- */

    protected void updateView(Wallet wallet) {
        // uid
        mUidView.setText(wallet.getUid());

        // Icon
        mIcon.setImageResource(ImageUtils.getImageWhite(wallet));

        // Registration date
        if (wallet.getCertTimestamp() > 0) {
            mTimestampLabelView.setText(R.string.registration_date);
            mTimestampView.setText(DateUtils.format(wallet.getCertTimestamp()));
        }
        else {
            mTimestampLabelView.setText(getString(R.string.not_registred));
            mTimestampView.setText("");
        }

        // Pub key
        {
            String pubkey = wallet.getPubKeyHash();
            int offset = pubkey.length()/2;
            pubkey = pubkey.substring(0, offset) + '\n' + pubkey.substring(offset);
            mPubkeyView.setText(pubkey);
        }

        // Currency
        mCurrencyView.setText(wallet.getCurrency());

        // If unit is coins
        if (SettingsActivity.PREF_UNIT_COIN.equals(mUnitType)) {
            // Credit as coins
            mCreditView.setText(CurrencyUtils.formatCoin(wallet.getCredit()));
        }

        // If unit is UD
        else if (SettingsActivity.PREF_UNIT_UD.equals(mUnitType)) {
            // Credit as UD
            mCreditView.setText(getString(
                    R.string.universal_dividend_value,
                    CurrencyUtils.formatUD(wallet.getCreditAsUD())));
        }

        // Other unit
        else {
            mCreditView.setVisibility(View.GONE);
        }

        // Use the pre-loaded WOT data if exists
        if (CollectionUtils.isNotEmpty(wallet.getCertifications())) {
            mCertificationListAdapter.clear();
            mCertificationListAdapter.addAll(wallet.getCertifications());
            mCertificationListAdapter.notifyDataSetChanged();
            mProgressViewAdapter.showProgress(false);
        }

        // Load WOT data
        else {
            LoadTask task = new LoadTask();
            task.execute(wallet);
        }
    }

    protected void onTransferClick() {
        Bundle newInstanceArgs = getArguments();
        Wallet wallet = (Wallet)
                newInstanceArgs.getSerializable(Wallet.class.getSimpleName());

        Fragment fragment = TransferFragment.newInstance(wallet);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onSelfClick() {
        // Retrieve wallet
        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

        // Retrieve the fragment to pop after self certification
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 0);

        // Launch the self certification
        LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
            public void onSuccess(Wallet authWallet) {
                SelfCertificationTask task = new SelfCertificationTask(popBackStackName);
                task.execute(authWallet);
            }
        });
    }

    protected void onRequestMembershipClick() {
        // Retrieve wallet
        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

        // Retrieve the fragment to pop after transfer
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 0);

        // Perform the join (after login)
        LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
            public void onSuccess(Wallet authWallet) {
                RequestMembershipTask task = new RequestMembershipTask(popBackStackName);
                task.execute(authWallet);
            }
        });
    }

    protected void onWotIdentityClick(int position) {

        // Get certification
        WotCertification cert = (WotCertification) mCertificationListAdapter
                .getItem(position);

        Fragment fragment = IdentityFragment.newInstance(cert, mTabs.getCurrentTab());
        FragmentManager fragmentManager = getFragmentManager();

        /*fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right,
                        R.animator.slide_out_left)
                .remove(WalletFragment.this)
                .commit();*/

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right,
                        R.animator.slide_out_left,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onDeleteClick() {
        // Retrieve wallet
        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

        // Retrieve the fragment to pop after deleteion
        FragmentManager fragmentManager = getFragmentManager();
        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(
                fragmentManager.getBackStackEntryCount() - 2);
        final String popBackStackName = backStackEntry.getName();

        // Show confirmation dialog
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.delete_wallet))
                .setMessage(getString(R.string.delete_wallet_confirm))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Run the delete task
                        DeleteTask deleteTask = new DeleteTask(popBackStackName);
                        deleteTask.execute(wallet);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    protected void onError(Throwable error) {
        Toast.makeText(getActivity(),
                "Error: " + ExceptionUtils.getMessage(error),
                Toast.LENGTH_SHORT).show();

    }

    public class LoadTask extends AsyncTaskHandleException<Wallet, Void, Collection<WotCertification>> {

        public LoadTask() {
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Collection<WotCertification> doInBackgroundHandleException(Wallet... wallets) {
            Wallet wallet = wallets[0];

            // Get certifications (if has a uid)
            Collection<WotCertification> certifications = null;
            if (StringUtils.isNotBlank(wallet.getUid())) {
                WotRemoteService wotService = ServiceLocator.instance().getWotRemoteService();
                certifications =  wotService.getCertifications(
                        wallet.getCurrencyId(),
                        wallet.getUid(),
                        wallet.getPubKeyHash(),
                        wallet.getIdentity().isMember());
            }

            // Update the wallet( to avoid a new load when navigate on community members)
            wallet.setCertifications(certifications);

            return certifications;
         }

        @Override
        protected void onSuccess(Collection<WotCertification> certifications) {

            // Update certification list
            mCertificationListAdapter.clear();
            if (CollectionUtils.isNotEmpty(certifications)) {
                mCertificationListAdapter.addAll(certifications);
                mCertificationListAdapter.notifyDataSetChanged();
            }

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mCertificationListAdapter.clear();
            mProgressViewAdapter.showProgress(false);
            onError(t);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

    public class SelfCertificationTask extends AsyncTaskHandleException<Wallet, Void, Wallet> {

        private String popStackTraceName;

        public SelfCertificationTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            ViewUtils.hideKeyboard(getActivity());

            // Show the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Wallet doInBackgroundHandleException(Wallet... wallets) {
            Wallet wallet = wallets[0];

            // Get certifications (if has a uid)
            if (StringUtils.isNotBlank(wallet.getUid())) {
                ServiceLocator.instance().getWalletService().sendSelfAndSave(getContext(), wallet);

                return wallet;
            }
            else {
                return null;
            }
        }

        @Override
        protected void onSuccess(Wallet wallet) {
            mProgressViewAdapter.showProgress(false);
            if (wallet == null || wallet.getCertTimestamp() <= 0) {
                Toast.makeText(getContext(),
                        getString(R.string.join_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

                Toast.makeText(getContext(),
                        getString(R.string.join_sended),
                        Toast.LENGTH_LONG).show();

                updateView(wallet);
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Log.d(TAG, "Could not send join: " + ExceptionUtils.getMessage(error), error);
            Toast.makeText(getContext(),
                    getString(R.string.join_error)
                            + "\n"
                            + ExceptionUtils.getMessage(error),
                    Toast.LENGTH_SHORT).show();

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

    public class RequestMembershipTask extends AsyncTaskHandleException<Wallet, Void, Wallet> {

        private Activity mActivity = getActivity();
        private String popStackTraceName;

        public RequestMembershipTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            ViewUtils.hideKeyboard(mActivity);

            // Show the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Wallet doInBackgroundHandleException(Wallet... wallets) {
            Wallet wallet = wallets[0];

            // Get certifications (if has a uid)
            if (StringUtils.isNotBlank(wallet.getUid())
                    && wallet.isAuthenticate()) {
                ServiceLocator.instance().getBlockchainRemoteService().requestMembership(wallet);

                return wallet;
            }
            else {
                return null;
            }
        }

        @Override
        protected void onSuccess(Wallet wallet) {
            mProgressViewAdapter.showProgress(false);
            if (wallet == null || wallet.getCertTimestamp() <= 0) {
                Toast.makeText(mActivity,
                        getString(R.string.join_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

                Toast.makeText(mActivity,
                        getString(R.string.join_sended),
                        Toast.LENGTH_LONG).show();

                updateView(wallet);
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Log.d(TAG, "Could not send join: " + ExceptionUtils.getMessage(error), error);
            Toast.makeText(mActivity,
                    getString(R.string.join_error)
                            + "\n"
                            + ExceptionUtils.getMessage(error),
                    Toast.LENGTH_SHORT).show();

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

    public class DeleteTask extends AsyncTaskHandleException<Wallet, Void, Void> {

        private String popStackTraceName;

        public DeleteTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Void doInBackgroundHandleException(Wallet... wallets) {
            Wallet wallet = wallets[0];

            // Do deletion
            ServiceLocator.instance().getWalletService().delete(getContext(), wallet.getId());

            return (Void)null;
        }

        @Override
        protected void onSuccess(Void args) {
            mProgressViewAdapter.showProgress(false);
            getFragmentManager().popBackStack(popStackTraceName, 0); // return back

            Toast.makeText(getContext(),
                    getString(R.string.wallet_deleted),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Log.d(TAG, "Could not delete wallet: " + ExceptionUtils.getMessage(error), error);
            Toast.makeText(getContext(),
                    getString(R.string.delete_wallet_error, ExceptionUtils.getMessage(error)),
                            Toast.LENGTH_SHORT).show();

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }
}
