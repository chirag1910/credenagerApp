package com.credenager.fragments;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.credenager.HomeActivity;
import com.credenager.R;
import com.credenager.dialogs.ConfirmationDialog;
import com.credenager.utils.Api;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Calendar;
import java.util.concurrent.Executor;

public class KeyPageFragment extends Fragment {
    private final float startX, startY;
    private EditText keyEdittext;
    private ExtendedFloatingActionButton validateButton;
    private TextView emailIndicator, forgotKeyLink, logoutLink;

    public KeyPageFragment(float x, float y) {
        startX = x;
        startY = y;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.key_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View keyTop = view.findViewById(R.id.key_top);
        emailIndicator = view.findViewById(R.id.key_email_indicator);
        keyEdittext = view.findViewById(R.id.key_key_edittext);
        validateButton = view.findViewById(R.id.key_validate_button);
        forgotKeyLink = view.findViewById(R.id.forgot_key_link);
        logoutLink = view.findViewById(R.id.key_logout_link);

        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        emailIndicator.setText(
                "Logged in as ".concat(Session.USER_EMAIL) + (Session.APP_OFFLINE_MODE ? " - Offline Mode" : "")
        );

        if (startY >= 0 && startX >= 0) {
            int[] coordinates = new int[2];
            keyTop.getLocationOnScreen(coordinates);
            keyTop.setTranslationY((startY - Globals.dpToPx(70, requireContext())) - coordinates[1]);
            keyTop.setTranslationX(startX - coordinates[0]);
            keyTop.setVisibility(View.VISIBLE);
            keyTop.animate().setInterpolator(new DecelerateInterpolator()).translationY(0).translationX(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {}

                @Override
                public void onAnimationEnd(Animator animator) {
                    emailIndicator.setAlpha(0);
                    emailIndicator.setVisibility(View.VISIBLE);
                    keyEdittext.setScaleX(0);
                    keyEdittext.setScaleY(0);
                    keyEdittext.setVisibility(View.VISIBLE);
                    validateButton.setScaleX(0);
                    validateButton.setScaleY(0);
                    validateButton.setVisibility(View.VISIBLE);
                    forgotKeyLink.setAlpha(0);
                    forgotKeyLink.setVisibility(View.VISIBLE);
                    logoutLink.setAlpha(0);
                    logoutLink.setVisibility(View.VISIBLE);
                    emailIndicator.animate().alpha(1).start();
                    keyEdittext.animate().scaleX(1).scaleY(1).start();
                    validateButton.animate().scaleX(1).scaleY(1).start();
                    forgotKeyLink.animate().alpha(1).start();
                    logoutLink.animate().alpha(1).start();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).start();

        }
        else {
            keyTop.setVisibility(View.VISIBLE);
            emailIndicator.setVisibility(View.VISIBLE);
            keyEdittext.setVisibility(View.VISIBLE);
            validateButton.setVisibility(View.VISIBLE);
            forgotKeyLink.setVisibility(View.VISIBLE);
            logoutLink.setVisibility(View.VISIBLE);
        }

        validateButton.setOnClickListener((v) -> handleSubmit(v, false));
        logoutLink.setOnClickListener(this::handleLogout);
        forgotKeyLink.setOnClickListener(this::gotoResetKeyPage);

        checkRelatedSettings(view);
    }

    private void checkRelatedSettings(View view){
        String userKey = Globals.getKey(requireContext());
        long timeLimit = Globals.getAutomaticUnlockTimeout(requireContext());

        if (Session.USER_EMAIL.equals(Globals.DUMMY_ACCOUNT_EMAIL)){
            keyEdittext.setText(Globals.DUMMY_ACCOUNT_KEY);
            handleSubmit(view, true);
        } else if (userKey != null && timeLimit > Calendar.getInstance().getTimeInMillis()) {
            Boolean bypassKeyPageSetting = (Boolean) Globals.getSettings(requireContext()).getOrDefault(Globals.BYPASS_KEY_KEY, false);
            Boolean biometricSetting = (Boolean) Globals.getSettings(requireContext()).getOrDefault(Globals.BIOMETRIC_KEY, false);

            if (Boolean.TRUE.equals(bypassKeyPageSetting)) {
                keyEdittext.setText(userKey);
                handleSubmit(view, true);
            } else if (Boolean.TRUE.equals(biometricSetting)) {
                Executor executor = ContextCompat.getMainExecutor(requireContext());

                BiometricPrompt biometricPrompt = new BiometricPrompt(requireActivity(), executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            Toast.makeText(requireContext(), "Biometric authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        keyEdittext.setText(userKey);
                        handleSubmit(view, true);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(requireContext(), "Biometric authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });

                BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Authenticate")
                        .setSubtitle("Authenticate using your biometric credential")
                        .setNegativeButtonText("Use key instead?")
                        .setConfirmationRequired(false)
                        .build();

                showBiometricPrompt(biometricPrompt, promptInfo, 0);
            }
        }
    }

    private void showBiometricPrompt(BiometricPrompt biometricPrompt, BiometricPrompt.PromptInfo promptInfo, int iteration){
        if (iteration < 10){
            try{
                biometricPrompt.authenticate(promptInfo);
            } catch (Exception e){
                new Handler().postDelayed(() -> showBiometricPrompt(biometricPrompt, promptInfo, iteration+1), 100);
            }
        }
    }

    private void handleSubmit(View view, boolean automatic) {
        final String key = keyEdittext.getText().toString();

        if (key.isEmpty()){
            Toast.makeText(getContext(), "Key Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        Api.verifyKey(Session.JWT_TOKEN, key, response -> {
            if (getContext() == null) return;

            try{
                int responseCode = response.getInt("code");
                if (responseCode == 200) {
                    if (!automatic) Globals.saveAutomaticUnlockValue(requireContext());

                    Globals.saveKey(requireContext(), key);
                    Session.setKey(key);
                    new Handler(Looper.getMainLooper()).post(this::gotoHomePage);
                }
                else if (responseCode == 502) {
                    String storedKey = Globals.getKey(requireContext());

                    if (Session.APP_OFFLINE_MODE) {
                        if (storedKey.equals(key)) {
                            Session.setKey(key);
                            new Handler(Looper.getMainLooper()).post(this::gotoHomePage);
                        }
                        else {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(getContext(), "Invalid Key", Toast.LENGTH_LONG).show()
                            );
                        }
                    }
                    else{
                        String error = response.getString("error");
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                        );
                    }
                }
                else{
                    String error = response.getString("error");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                    );
                }
            } catch (Exception e){
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), "Unknown Error Occurred!", Toast.LENGTH_LONG).show()
                );
            }
            new Handler(Looper.getMainLooper()).post(() -> enableButtons(true));
        });
    }

    private void handleLogout(View view) {
        new ConfirmationDialog(requireContext(), "Logout?", "Logout", result -> {
            if (result) {
                Globals.logout(requireContext());
                gotoLoginPage();
            }
        }).show();
    }

    private void enableButtons (boolean enable){
        validateButton.setEnabled(enable);

        if (!enable)
            validateButton.setAlpha(0.3f);
        else
            validateButton.setAlpha(1);
    }

    private void gotoHomePage() {
        requireActivity().startActivity(new Intent(requireActivity(), HomeActivity.class));
        requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_from_left);
        requireActivity().finish();
    }

    private void gotoLoginPage() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                .replace(R.id.fragment_container, new LoginPageFragment(), Globals.LOGIN_FRAGMENT_TAG)
                .commit();
    }

    private void gotoResetKeyPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                .replace(R.id.fragment_container, new ResetKeyPageFragment(), Globals.RESET_KEY_FRAGMENT_TAG)
                .commit();
    }
}
